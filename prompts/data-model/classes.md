# Data Model Classes

## Event
Interface representing a generic Kafka Event

### Fields
- eventTime: Instant

## Transaction
Interface representing any financial transaction.

### Fields
- amount: double
- currency: Currency

## BankTransaction
Class representing a basic banking transaction.

### Implements
- Transaction
- Event

### Fields
- amount: double
- currency: Currency
- fromAccount: BankAccount
- toAccount: BankAccount

## Bank Account

### Fields
- accountName: String
- ownerName: String
- institutionName: String