# Project Walkthrough

## 30-second explanation

This project is a backend order-risk system for e-commerce. A Spring Boot API accepts customer orders, stores them in PostgreSQL, calculates customer/transaction signals such as account age and recent order frequency, and sends those inputs to a Python rule engine. The engine returns a numerical score, LOW/MEDIUM/HIGH classification, and the exact reasons that triggered the score.

## Why Java + Spring Boot?

Java is widely used for enterprise backend systems. Spring Boot provides clear controller/service/repository separation, dependency injection, validation, and database integration through JPA.

## Why PostgreSQL?

The data is relational: customers have many orders, and orders have risk results/reasons. PostgreSQL provides durable relational persistence, constraints, and SQL querying.

## Why Python?

Python keeps the scoring rules easy to read, test, and change. Separating the risk engine also demonstrates integrating components written in different languages.

## Request flow

1. Client posts an order.
2. Spring Boot loads the customer from PostgreSQL.
3. The service calculates account age and counts orders from the last 24 hours.
4. The service determines whether billing and shipping addresses differ.
5. Those features are passed to the Python scorer.
6. Python returns score, level, and triggered reasons.
7. Spring Boot persists the scored order and returns the result as JSON.

## Interview questions to be ready for

- Why use a rule-based scorer instead of machine learning?
- How do your database relationships work?
- What is the controller/service/repository pattern?
- How do you validate API input?
- How would you deploy this to AWS?
- How would you protect sensitive customer information?
- How would you replace the Python subprocess with a production service?

## Strong answer about ML

I started with deterministic rules because they are explainable and do not require a labeled fraud dataset. If enough labeled order history became available, I could compare the rule system against a model and use precision/recall to decide whether machine learning actually improves detection.

## Testing strategy

The project has three testing layers:

1. Python unit tests verify low-, medium-, and high-risk scoring behavior.
2. Java service tests isolate order-processing logic with mocked dependencies.
3. A Spring Boot API integration test creates a customer, submits an order, invokes the real Python risk engine, and verifies the returned risk classification using an in-memory H2 database.

## Docker and CI

`docker compose up --build` starts both PostgreSQL and the Spring Boot API. The application container includes Python so the same risk engine runs inside the container.

GitHub Actions runs the Python tests and Maven test suite on every push and pull request. This provides a simple CI pipeline without adding unnecessary production infrastructure to a student portfolio project.
