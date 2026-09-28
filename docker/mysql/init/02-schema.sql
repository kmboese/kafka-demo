-- Runs against MYSQL_DATABASE (transaction-processor) on first start.

CREATE TABLE IF NOT EXISTS bank_account (
    id               INT          NOT NULL AUTO_INCREMENT,
    account_name     VARCHAR(255) NOT NULL,
    owner_name       VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_bank_account_natural_key UNIQUE (institution_name, owner_name, account_name)
);

CREATE TABLE IF NOT EXISTS bank_transactions (
    id              INT       NOT NULL AUTO_INCREMENT,
    event_time      TIMESTAMP(6) NOT NULL,
    amount          DOUBLE    NOT NULL,
    currency_code   CHAR(3)   NOT NULL,
    from_account_id INT       NOT NULL,
    to_account_id   INT       NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bank_transactions_from_account FOREIGN KEY (from_account_id) REFERENCES bank_account (id),
    CONSTRAINT fk_bank_transactions_to_account   FOREIGN KEY (to_account_id)   REFERENCES bank_account (id)
);
