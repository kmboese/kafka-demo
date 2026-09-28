# Project Setup
The goal of this project is to create a mock financial transactions processing app that uses Kafka in it to demonstrate basic Kafka concepts.

## References
- [Deployment Instructions](deployment.md)
- [Data Model Instructions](../data-model)
- [Database Setup](../database/database.md)

## Language
This project will use Java, including the Spring Boot framework for creating app servers, interacting with databases, etc.

## Tech Stack
- Language: Java 27
- Server: Spring Boot 4.1.1
- Database: MySQL 9.7 LTS
- Messaging: Kafka 4.3
- Containerization: Docker engine v29

## Containerization
I want to containerize all servers using Docker and a Docker Compose file. This will allow me to easily deploy the app locally in many environments.

## Project Setup
- MySQL
  - See database setup
- Kafka
  - Topic: `bank-transactions`
- `api-gateway` app: handles inbound HTTP calls
  - Spring Boot app server
  - Routes inbound requests
    - Requests of type `BankTransaction` get routed to the `bank-transactions` topic
- `bank-db-consumer`: 
  - Spring Boot app server
  - Subscribes to the `bank-transactions` topic
    - On event:
      - Serializes message to `BankTransaction` entity
      - Saves the entity to the `bank-transactions` MySQL table
- `bank-logging-consumer`
  - Spring Boot app server
  - Subscribes to the `bank-transactions` topic
    - On event:
      - Serializes message to `BankTransaction` entity
      - Writes the serialized output to a CSV log file: `./logs/bank-transactions/log-{YYYY}-{MM}-{DD}-{HHMMSS}.csv`
        - For now, write every transaction event to the log. We can optimize this in the future to batch transactions before writing to the log to make I/O more efficient. 

## Things to Investigate

### Kafka
- Topic partitioning
- Message ordering with message keys