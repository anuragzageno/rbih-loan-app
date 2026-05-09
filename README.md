# rbih-loan-app

A Spring Boot REST service that evaluates loan applications and determines whether a single loan offer can be approved based on requested tenure.

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Data JPA** with H2 in-memory database
- **Spring Validation** (JSR-380 / Bean Validation)
- **Lombok**
- **JUnit 5** / Spring Boot Test

## Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

The H2 console is available at `http://localhost:8080/h2-console`  
(JDBC URL: `jdbc:h2:mem:loandb`, username: `sa`, no password).

## Running Tests

```bash
mvn test
```

## API Overview

| Method | Endpoint         | Description                        |
|--------|------------------|------------------------------------|
| POST   | `/applications`  | Submit and evaluate a loan request |
