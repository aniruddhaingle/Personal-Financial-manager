# Personal Finance Manager REST API

A production-quality, session-secure REST API for managing personal finance entries (incomes, expenses, categories, savings goals, and reporting statistics). Built with **Java 17**, **Spring Boot 3.x**, and **Spring Security**.

---

## Technical Stack
- **Core**: Java 17, Spring Boot 3.2.5
- **Security**: Spring Security (Session-based Cookie Authentication, BCrypt password hashing)
- **Data & Persistence**: Spring Data JPA, H2 Database (In-Memory)
- **Utilities & Tooling**: Lombok, Actuator, Springdoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, Mockito, MockMvc

---

## Clean Layered Architecture

The project enforces strict separation of concerns following a layered architecture:

```
                  ┌──────────────────────┐
                  │      HTTP Client     │
                  └──────────┬───────────┘
                             │ Cookie Session (JSESSIONID)
                             ▼
                  ┌──────────────────────┐
                  │    REST Controller   │  <-- DTOs & Validation
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │    Service Layer     │  <-- Core Logic & @Transactional
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │   Repository Layer   │  <-- JPA Queries & Indexes
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │     H2 Database      │
                  └──────────────────────┘
```

### Key Engineering Practices Incorporated:
1. **Audited Abstraction**: Shared audited base configurations (`createdAt`, `updatedAt`) using JPA `@MappedSuperclass` mapping (`BaseEntity.java`).
2. **DTO Isolation**: Request and response objects (`*Dto.java`) act as boundaries, ensuring entity classes never escape to web endpoints.
3. **DTO-Only Validation**: Validation constraints (`@NotBlank`, `@Email`, `@Positive`, `@PastOrPresent`, `@Future`) are placed strictly on request DTO classes, keeping database entity classes decoupled.
4. **Data Isolation**: Multi-tenant data integrity is secured by resolving the currently authenticated session username and executing isolated JPA queries (`WHERE user.id = :userId`).
5. **Programmatic Session Security**: Logins perform programmatic authentication using the `AuthenticationManager` and associate credentials with standard servlet-container sessions (`JSESSIONID`).
6. **No Duplicate Transaction Types**: Rather than storing category types redundantly, a transaction's type is dynamically resolved via `category.getType()` using `CategoryType { INCOME, EXPENSE }`.
7. **Database Indexing**: Crucial queries (filtering, aggregations) are backed by database indexes on chronological fields and isolated keys.

---

## API Documentation

All protected APIs require an active authenticated session. On login, a secure HttpOnly session cookie (`JSESSIONID`) is set by the browser.

### 1. Authentication & User Management
- **`POST /api/auth/register`**: Register a new user.
  - Payload:
    ```json
    {
      "username": "user@syfe.com",
      "password": "securePassword123",
      "fullName": "John Doe",
      "phoneNumber": "+1234567890"
    }
    ```
- **`POST /api/auth/login`**: Authenticate and retrieve session cookie.
  - Payload:
    ```json
    {
      "username": "user@syfe.com",
      "password": "securePassword123"
    }
    ```
- **`POST /api/auth/logout`**: Terminate session.

### 2. Category Management
- **`POST /api/categories`**: Create a custom category.
  - Payload:
    ```json
    {
      "name": "Freelance",
      "type": "INCOME"
    }
    ```
- **`GET /api/categories`**: Retrieve all categories available to the user (global default categories + user custom categories).
- **`DELETE /api/categories/{id}`**: Delete a custom category. (Blocks deletion if category is a global default, is owned by another user, or is currently linked to active transactions).

### 3. Transaction Management
- **`POST /api/transactions`**: Create a transaction.
  - Payload:
    ```json
    {
      "amount": 2500.50,
      "date": "2026-05-23",
      "categoryId": 1,
      "description": "Consulting work"
    }
    ```
- **`GET /api/transactions`**: Retrieve paginated, sorted, and filtered transactions.
  - Optional Query Parameters: `startDate`, `endDate`, `categoryName`, `categoryType`, `page`, `size`, `sort`.
  - Example: `GET /api/transactions?categoryType=EXPENSE&page=0&size=5&sort=date,desc`
- **`PUT /api/transactions/{id}`**: Update transaction details (amount, category, description). (Date is immutable and cannot be updated).
- **`DELETE /api/transactions/{id}`**: Delete a transaction.

### 4. Savings Goals
- **`POST /api/savings-goals`**: Create a savings goal.
  - Payload:
    ```json
    {
      "goalName": "Emergency Fund",
      "targetAmount": 10000.00,
      "startDate": "2026-05-01",
      "targetDate": "2026-12-31"
    }
    ```
- **`GET /api/savings-goals`**: List all goals with dynamic progress tracking.
  - Returns `currentProgress` (Income - Expenses since `startDate`), `progressPercentage`, and `remainingAmount`.
- **`GET /api/savings-goals/{id}`**: Get progress details for a specific goal.
- **`PUT /api/savings-goals/{id}`**: Update savings goal properties.
- **`DELETE /api/savings-goals/{id}`**: Delete a savings goal.

### 5. Dynamic Reports
- **`GET /api/reports/monthly`**: Grouped incomes, expenses, and net savings for a month.
  - Query parameters: `year=2026&month=5`
- **`GET /api/reports/yearly`**: Yearly grouped summary.
  - Query parameters: `year=2026`

### 6. Swagger API Documentation
- **Swagger UI**: Access comprehensive documentation at `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: Access the JSON schema at `http://localhost:8080/v3/api-docs`

### 7. Application Monitoring
- **Health Check**: Access state metrics at `http://localhost:8080/actuator/health`

---

## Local Setup Instructions

1. **Prerequisites**: Ensure you have **Java 17** and **Maven** installed.
2. **Clone and Build**:
   ```bash
   mvn clean package
   ```
3. **Run Locally**:
   ```bash
   mvn spring-boot:run
   ```
4. **H2 Console Access**: Access the development DB console at `http://localhost:8080/h2-console` using:
   - **JDBC URL**: `jdbc:h2:mem:personalfinancedb`
   - **Username**: `sa`
   - **Password**: *(Leave empty)*

---

## Testing Instructions

### Automated Tests
To run unit and MockMvc integration test profiles:
```bash
mvn clean test
```

### Manual Verification Flow (using curl)
1. **Register**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user@syfe.com","password":"password123","fullName":"John Doe"}'
   ```
2. **Login & Save Session**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"user@syfe.com","password":"password123"}' -c cookies.txt
   ```
3. **Get Available Categories**:
   ```bash
   curl -X GET http://localhost:8080/api/categories -b cookies.txt
   ```
4. **Create Transaction**:
   ```bash
   curl -X POST http://localhost:8080/api/transactions -b cookies.txt -H "Content-Type: application/json" -d '{"amount":500.00,"date":"2026-05-23","categoryId":2,"description":"Dinner at Restaurant"}'
   ```
5. **Get Paginated Transactions**:
   ```bash
   curl -X GET "http://localhost:8080/api/transactions?page=0&size=10&sort=date,desc" -b cookies.txt
   ```

---

## Render Deployment Guide

### Deployment Runtime Environment Variables
Ensure the following settings are configured on your Render Web Service dashboard:

| Variable | Recommended Value | Description |
| :--- | :--- | :--- |
| `JAVA_VERSION` | `17` | Directs Render to build with JDK 17 |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production properties (disables H2 console and SQL console logs) |
| `COOKIE_SECURE` | `true` | Enforces `Secure` flag on cookies under Render HTTPS routes |
| `PORT` | `8080` (or leave empty) | Custom Web Service port |

### Build & Start Command Settings
- **Build Command**:
  ```bash
  mvn clean package -DskipTests
  ```
- **Start Command**:
  ```bash
  java -jar target/personal-finance-0.0.1-SNAPSHOT.jar
  ```
