# Database Setup
- Database name: transaction-processor
- Port: 3306
- Admin user
  - Username: admin
  - Password: {{MYSQL_ADMIN_PASSWORD}} in [.env](../../.env)
- App User
  - Username: app_user
  - Password: {{MYSQL_APP_USER_PASSWORD}} in [.env](../../.env)

## Table: bank-transactions
Holds the `BankTransaction` entity.
- id: INT NOT NULL AUTO_INCREMENT
- eventTime: TIMESTAMP
- amount: DOUBLE
- currency_code: CHAR(3)
- fromAccount: reference to matching `bank-account`, matching on `{institution_name}` + `{ownerName}`
- toAccount: reference to `bank-account` ID, matching on `{institution_name}` + `{ownerName}`
- created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

## Table: bank-account
- id: INT NOT NULL AUTO_INCREMENT
- account_name: VARCHAR(255)
- owner_name: VARCHAR(255)
- institution_name: VARCHAR(255)
- created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
