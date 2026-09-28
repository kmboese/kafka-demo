# kafka-demo
Application demoing functionality of Apache Kafka

## Running locally

Requires Docker. Put `MYSQL_ADMIN_PASSWORD` and `MYSQL_APP_USER_PASSWORD` in `.env`, then:

```sh
docker compose up -d --build
# If a local MySQL already uses port 3306:
MYSQL_HOST_PORT=3308 docker compose up -d --build
```

| Service      | Host address            |
|--------------|-------------------------|
| api-gateway  | http://localhost:8080   |
| Kafka        | localhost:9094          |
| MySQL        | localhost:3306 (`transaction-processor`) |

Send a transaction:

```sh
curl -X POST localhost:8080/api/bank-transactions -H 'Content-Type: application/json' -d '{
  "amount": 125.50, "currency": "USD",
  "fromAccount": {"accountName": "Checking", "ownerName": "Alice", "institutionName": "First Bank"},
  "toAccount":   {"accountName": "Savings",  "ownerName": "Bob",   "institutionName": "Second Bank"}
}'
```

Build and test without Docker: `./gradlew build` (Gradle downloads a Java 25 toolchain if needed).
