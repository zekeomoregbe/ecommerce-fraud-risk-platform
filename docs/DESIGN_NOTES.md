# Design Notes

## Service boundaries

The Spring Boot application owns the HTTP API, validation, persistence, and order-processing workflow. The Python module is responsible only for turning calculated order signals into a risk score and classification.

The Python scorer currently runs as a local process. That keeps the project simple while preserving a clear boundary between order processing and risk logic. In a larger system, the scorer could be moved behind its own service endpoint or message queue.

## Order scoring flow

When an order is created:

1. The customer is loaded from the database.
2. The service calculates the age of the customer account.
3. Orders for the same customer in the previous 24 hours are counted.
4. Billing and shipping addresses are normalized and compared.
5. The inputs are passed to the Python scorer.
6. The returned score, level, and triggered reasons are stored with the order.

Updating an order re-runs the scoring logic. The existing order is excluded from the recent-order count so an update does not inflate transaction velocity.

## Scoring model

The scorer is rule-based rather than statistical. Each signal contributes a fixed number of points and the total is capped at 100.

Current signals:

- order value
- account age
- recent order frequency
- billing/shipping mismatch

This makes scoring predictable and easy to test. It also avoids pretending that the system has a trained fraud model without a labeled fraud dataset.

## Persistence

Customers and orders are stored with JPA entities backed by PostgreSQL.

A customer can have multiple orders. Risk reasons are stored as an element collection associated with each order, which keeps the current schema small while still preserving the reason list returned by the scorer.

## Validation and errors

Request DTOs use Jakarta Bean Validation for required fields, email format, positive order values, and account creation dates.

A global exception handler converts validation failures, missing records, duplicate emails, and unexpected errors into consistent JSON responses.

## Testing

Python unit tests cover the main scoring bands.

The Java service test isolates order-processing behavior with mocked dependencies. The API integration test runs Spring Boot with an H2 database and calls the real Python scorer to verify the full request path without requiring PostgreSQL in CI.

## Current limitations

- The scoring rules are intentionally simple and are not a substitute for a production fraud model.
- Address comparison is text-based rather than using address verification or normalization services.
- The Python scorer runs as a subprocess instead of an independently deployed service.
- The project does not currently include authentication or authorization.
- PostgreSQL schema changes rely on Hibernate's update mode rather than versioned migrations.
