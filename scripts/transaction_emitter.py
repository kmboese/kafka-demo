#!/usr/bin/env python3
"""Send mock bank transactions from a CSV file to the api-gateway.

Usage:
    python3 scripts/transaction_emitter.py --inputFile sample-transactions.csv
    python3 scripts/transaction_emitter.py --inputFile sample-transactions.csv \
        --transactionDelay 200 --concurrency 4

A bare file name is looked up in scripts/input-data/. The whole file is validated
before anything is sent; any invalid row logs an error and exits without sending.

Expected CSV header (event_time is optional, ISO-8601; the gateway fills it in if blank):
    amount,currency,from_account_name,from_owner_name,from_institution_name,
    to_account_name,to_owner_name,to_institution_name[,event_time]

Uses only the Python standard library.
"""

from __future__ import annotations

import argparse
import csv
import json
import logging
import re
import sys
import threading
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path

INPUT_DATA_DIR = Path(__file__).resolve().parent / "input-data"
DEFAULT_GATEWAY_URL = "http://localhost:8080/api/bank-transactions"

REQUIRED_COLUMNS = [
    "amount",
    "currency",
    "from_account_name",
    "from_owner_name",
    "from_institution_name",
    "to_account_name",
    "to_owner_name",
    "to_institution_name",
]
OPTIONAL_COLUMNS = ["event_time"]
CURRENCY_PATTERN = re.compile(r"^[A-Z]{3}$")

log = logging.getLogger("transaction_emitter")


@dataclass(frozen=True)
class BankAccount:
    accountName: str
    ownerName: str
    institutionName: str


@dataclass(frozen=True)
class BankTransaction:
    amount: float
    currency: str
    fromAccount: BankAccount
    toAccount: BankAccount
    eventTime: str | None = None

    def to_json(self) -> bytes:
        body = asdict(self)
        if body["eventTime"] is None:
            del body["eventTime"]
        return json.dumps(body).encode("utf-8")


class InvalidInputError(Exception):
    pass


def resolve_input_file(input_file: str) -> Path:
    path = Path(input_file)
    if path.is_file():
        return path
    fallback = INPUT_DATA_DIR / input_file
    if fallback.is_file():
        return fallback
    raise InvalidInputError(f"Input file not found: {input_file} (also looked in {INPUT_DATA_DIR})")


def _required_text(row: dict, column: str, line: int) -> str:
    value = (row.get(column) or "").strip()
    if not value:
        raise InvalidInputError(f"line {line}: '{column}' is empty")
    if len(value) > 255:
        raise InvalidInputError(f"line {line}: '{column}' is longer than 255 characters")
    return value


def parse_row(row: dict, line: int) -> BankTransaction:
    if None in row:
        raise InvalidInputError(f"line {line}: more values than header columns")

    raw_amount = _required_text(row, "amount", line)
    try:
        amount = float(raw_amount)
    except ValueError:
        raise InvalidInputError(f"line {line}: amount '{raw_amount}' is not a number") from None
    if not amount > 0:
        raise InvalidInputError(f"line {line}: amount must be positive, got {raw_amount}")

    currency = _required_text(row, "currency", line).upper()
    if not CURRENCY_PATTERN.match(currency):
        raise InvalidInputError(f"line {line}: currency '{currency}' is not a 3-letter ISO 4217 code")

    event_time = (row.get("event_time") or "").strip() or None
    if event_time is not None:
        try:
            datetime.fromisoformat(event_time.replace("Z", "+00:00"))
        except ValueError:
            raise InvalidInputError(f"line {line}: event_time '{event_time}' is not ISO-8601") from None

    return BankTransaction(
        amount=amount,
        currency=currency,
        fromAccount=BankAccount(
            accountName=_required_text(row, "from_account_name", line),
            ownerName=_required_text(row, "from_owner_name", line),
            institutionName=_required_text(row, "from_institution_name", line),
        ),
        toAccount=BankAccount(
            accountName=_required_text(row, "to_account_name", line),
            ownerName=_required_text(row, "to_owner_name", line),
            institutionName=_required_text(row, "to_institution_name", line),
        ),
        eventTime=event_time,
    )


def load_transactions(path: Path) -> list[BankTransaction]:
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        header = [c.strip() for c in (reader.fieldnames or [])]
        missing = [c for c in REQUIRED_COLUMNS if c not in header]
        unknown = [c for c in header if c not in REQUIRED_COLUMNS + OPTIONAL_COLUMNS]
        if missing or unknown:
            raise InvalidInputError(
                f"bad CSV header in {path}: missing={missing or 'none'} unknown={unknown or 'none'}; "
                f"expected {','.join(REQUIRED_COLUMNS)}[,event_time]"
            )
        reader.fieldnames = header

        transactions = [parse_row(row, reader.line_num) for row in reader]

    if not transactions:
        raise InvalidInputError(f"{path} has no transaction rows")
    return transactions


def post_transaction(url: str, index: int, transaction: BankTransaction, timeout: float) -> bool:
    request = urllib.request.Request(
        url,
        data=transaction.to_json(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            result = json.loads(response.read() or b"{}")
            log.info(
                "#%d %s %.2f %s -> partition %s offset %s",
                index, transaction.fromAccount.ownerName, transaction.amount, transaction.currency,
                result.get("partition"), result.get("offset"),
            )
            return True
    except urllib.error.HTTPError as e:
        log.error("#%d rejected: HTTP %d %s", index, e.code, e.read().decode("utf-8", "replace"))
    except (urllib.error.URLError, TimeoutError) as e:
        log.error("#%d failed: %s", index, getattr(e, "reason", e))
    return False


def emit(transactions: list[BankTransaction], url: str, delay_ms: int, concurrency: int, timeout: float) -> int:
    """Send transactions with at most `concurrency` in flight, waiting `delay_ms` between sends."""
    in_flight = threading.BoundedSemaphore(concurrency)
    results: list[bool] = []

    def send(index: int, transaction: BankTransaction) -> None:
        try:
            results.append(post_transaction(url, index, transaction, timeout))
        finally:
            in_flight.release()

    with ThreadPoolExecutor(max_workers=concurrency) as pool:
        for index, transaction in enumerate(transactions, start=1):
            in_flight.acquire()
            pool.submit(send, index, transaction)
            if delay_ms and index < len(transactions):
                time.sleep(delay_ms / 1000)

    failures = results.count(False)
    log.info("Sent %d transactions: %d accepted, %d failed", len(results), len(results) - failures, failures)
    return failures


def main() -> int:
    parser = argparse.ArgumentParser(description="Send bank transactions from a CSV file to the api-gateway.")
    parser.add_argument("--inputFile", required=True,
                        help="CSV file path, or a file name inside scripts/input-data/")
    parser.add_argument("--transactionDelay", type=int, default=50,
                        help="delay between transaction requests, in milliseconds (default: 50)")
    parser.add_argument("--concurrency", type=int, default=1,
                        help="maximum number of requests in flight at once (default: 1)")
    parser.add_argument("--gatewayUrl", default=DEFAULT_GATEWAY_URL,
                        help=f"api-gateway endpoint (default: {DEFAULT_GATEWAY_URL})")
    parser.add_argument("--timeout", type=float, default=15, help="per-request timeout in seconds (default: 15)")
    args = parser.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

    if args.transactionDelay < 0:
        parser.error("--transactionDelay must be >= 0")
    if args.concurrency < 1:
        parser.error("--concurrency must be >= 1")

    try:
        path = resolve_input_file(args.inputFile)
        transactions = load_transactions(path)
    except InvalidInputError as e:
        log.error("Invalid input: %s", e)
        return 1
    except (OSError, UnicodeDecodeError, csv.Error) as e:
        log.error("Could not read input file: %s", e)
        return 1

    log.info("Loaded %d transactions from %s; sending to %s (delay %d ms, concurrency %d)",
             len(transactions), path, args.gatewayUrl, args.transactionDelay, args.concurrency)
    failures = emit(transactions, args.gatewayUrl, args.transactionDelay, args.concurrency, args.timeout)
    return 0 if failures == 0 else 2


if __name__ == "__main__":
    sys.exit(main())
