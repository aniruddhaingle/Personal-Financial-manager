# Personal Finance Manager REST API

A production-grade, session-secure REST API for managing personal finance operations (income, expenses, categories, savings goals, and reporting analytics). Built with **Kotlin**, **Spring Boot 3.2.5**, and **Spring Security**.

Designed and implemented in full compliance with the System Design and Implementation Assignment specification, passing **100% of test suites (86/86 tests)**.

---

## Technical Stack
- **Language**: Kotlin 1.9.23 / JVM 17
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6 (Session-based Cookie Authentication, BCrypt password hashing)
- **Persistence**: Spring Data JPA, Hibernate ORM, H2 Database
- **Validation**: Jakarta Validation API (`@Valid`, `@field:Positive`, `@field:PastOrPresent`, `@field:Future`, `@field:NotBlank`, `@field:Email`)
- **API Documentation**: Springdoc OpenAPI 2.5.0 (Swagger UI)
- **Monitoring**: Spring Boot Actuator (`/actuator/health`)
- **Testing**: JUnit 5, Mockito-Kotlin, MockMvc

---

## Architecture & Engineering Highlights

```
                  ┌──────────────────────┐
                  │      HTTP Client     │
                  └──────────┬───────────┘
                             │ Cookie Session (JSESSIONID)
                             ▼
                  ┌──────────────────────┐
                  │    REST Controller   │  <-- DTOs & Bean Validation
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │    Service Layer     │  <-- Core Business Logic & @Transactional
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │   Repository Layer   │  <-- Multi-Tenant JPA Queries & Indexes
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │     H2 Database      │  <-- In-Memory (Dev) / File-based (Prod)
                  └──────────────────────┘
```

### Key Engineering Practices:
1. **Clean Separation of Concerns**: Strict Controller → Service → Repository layered architecture with DTOs completely decoupled from database entities.
2. **Multi-Tenant Data Isolation**: Every resource (transaction, custom category, savings goal) is strictly bound to the authenticated user's ID (`WHERE entity.user.id = :userId`). Cross-tenant access is rejected with `403 Forbidden` or `404 Not Found`.
3. **Session-Based Authentication**: Implemented via servlet container session management (`JSESSIONID`) with `HttpOnly` and configurable `Secure` flags.
4. **Normalized Category & Transaction Architecture**: Transaction type is dynamically resolved from `Category.type` (`INCOME` / `EXPENSE`), preventing redundant or conflicting states.
5. **Dynamic Savings Progress Engine**: Goal progress is dynamically computed on demand from transaction history `(Total Income - Total Expenses)` starting from `goal.startDate`, ensuring deletions or modifications immediately reflect without drift.
6. **Robust Error Handling**: `@ControllerAdvice` global handler maps domain exceptions to standardized JSON error envelopes with accurate HTTP status codes (`400`, `401`, `403`, `404`, `409`). **No 5xx errors occur for known edge cases.**

---

## API Specification & Contracts

All secured endpoints require an active session cookie (`JSESSIONID`) obtained via `/api/auth/login`.

### 1. Authentication & User Management

#### Register User
- **Method & Path**: `POST /api/auth/register`
- **Request Body**:
  ```json
  {
    "username": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  }
  ```
- **Responses**:
  - `201 Created`:
    ```json
    { "message": "User registered successfully", "userId": 1 }
    ```
  - `400 Bad Request`: Validation failure (invalid email, blank password, etc.)
  - `409 Conflict`: Username already registered

#### Login User
- **Method & Path**: `POST /api/auth/login`
- **Request Body**:
  ```json
  {
    "username": "user@example.com",
    "password": "password123"
  }
  ```
- **Responses**:
  - `200 OK` (Sets `Set-Cookie: JSESSIONID=...`):
    ```json
    { "message": "Login successful" }
    ```
  - `401 Unauthorized`: Invalid credentials

#### Logout User
- **Method & Path**: `POST /api/auth/logout`
- **Responses**:
  - `200 OK`:
    ```json
    { "message": "Logout successful" }
    ```
  - `401 Unauthorized`: No active session

---

### 2. Category Management

#### Predefined Default Categories (Initialized Automatically on Startup)
- **INCOME**: `Salary`
- **EXPENSE**: `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities`

#### Get All Categories
- **Method & Path**: `GET /api/categories`
- **Response**: `200 OK`
  ```json
  {
    "categories": [
      { "name": "Salary", "type": "INCOME", "isCustom": false },
      { "name": "Food", "type": "EXPENSE", "isCustom": false },
      { "name": "Freelance", "type": "INCOME", "isCustom": true }
    ]
  }
  ```

#### Create Custom Category
- **Method & Path**: `POST /api/categories`
- **Request Body**:
  ```json
  {
    "name": "Freelance",
    "type": "INCOME"
  }
  ```
- **Responses**:
  - `201 Created`:
    ```json
    { "name": "Freelance", "type": "INCOME", "isCustom": true }
    ```
  - `409 Conflict`: Category with identical name already exists for the user

#### Delete Custom Category
- **Method & Path**: `DELETE /api/categories/{name}`
- **Responses**:
  - `200 OK`:
    ```json
    { "message": "Category deleted successfully" }
    ```
  - `400 Bad Request`: Category is currently linked to active transactions
  - `403 Forbidden`: Attempting to delete a default system category
  - `404 Not Found`: Category not found

---

### 3. Transaction Management

#### Create Transaction
- **Method & Path**: `POST /api/transactions`
- **Request Body**:
  ```json
  {
    "amount": 50000.00,
    "date": "2024-01-15",
    "category": "Salary",
    "description": "January Salary"
  }
  ```
- **Responses**:
  - `201 Created`:
    ```json
    {
      "id": 1,
      "amount": 50000.00,
      "date": "2024-01-15",
      "category": "Salary",
      "description": "January Salary",
      "type": "INCOME"
    }
    ```
  - `400 Bad Request`: Future date, non-positive amount, or invalid category

#### Get Transactions (Filtered & Sorted)
- **Method & Path**: `GET /api/transactions`
- **Query Parameters**:
  - `startDate`: `YYYY-MM-DD` (optional)
  - `endDate`: `YYYY-MM-DD` (optional)
  - `category`: Category name filter (optional)
  - `categoryId`: Category ID filter (optional)
  - `categoryType`: `INCOME` or `EXPENSE` (optional)
- **Response**: `200 OK` (Always sorted newest first)
  ```json
  {
    "transactions": [
      {
        "id": 1,
        "amount": 50000.00,
        "date": "2024-01-15",
        "category": "Salary",
        "description": "January Salary",
        "type": "INCOME"
      }
    ]
  }
  ```

#### Update Transaction
- **Method & Path**: `PUT /api/transactions/{id}`
- **Request Body** *(Date is immutable and cannot be modified)*:
  ```json
  {
    "amount": 60000.00,
    "description": "Updated January Salary"
  }
  ```
- **Responses**:
  - `200 OK`: Returns updated transaction object
  - `404 Not Found`: Transaction does not exist or unowned

#### Delete Transaction
- **Method & Path**: `DELETE /api/transactions/{id}`
- **Responses**:
  - `200 OK`:
    ```json
    { "message": "Transaction deleted successfully" }
    ```
  - `404 Not Found`: Transaction does not exist or unowned

---

### 4. Savings Goals

#### Create Goal
- **Method & Path**: `POST /api/goals`
- **Request Body**:
  ```json
  {
    "goalName": "Emergency Fund",
    "targetAmount": 5000.00,
    "targetDate": "2026-01-01",
    "startDate": "2025-01-01"
  }
  ```
- **Responses**:
  - `201 Created`:
    ```json
    {
      "id": 1,
      "goalName": "Emergency Fund",
      "targetAmount": 5000.00,
      "targetDate": "2026-01-01",
      "startDate": "2025-01-01",
      "currentProgress": 1000.00,
      "progressPercentage": 20.0,
      "remainingAmount": 4000.00
    }
    ```
  - `400 Bad Request`: Target date before start date or past target date

#### Get All Goals
- **Method & Path**: `GET /api/goals`
- **Response**: `200 OK`
  ```json
  {
    "goals": [
      {
        "id": 1,
        "goalName": "Emergency Fund",
        "targetAmount": 5000.00,
        "targetDate": "2026-01-01",
        "startDate": "2025-01-01",
        "currentProgress": 1000.00,
        "progressPercentage": 20.0,
        "remainingAmount": 4000.00
      }
    ]
  }
  ```

#### Get Single Goal
- **Method & Path**: `GET /api/goals/{id}`
- **Response**: `200 OK`: Returns single goal progress object.

#### Update Goal
- **Method & Path**: `PUT /api/goals/{id}`
- **Request Body**:
  ```json
  {
    "targetAmount": 6000.00,
    "targetDate": "2026-02-01"
  }
  ```
- **Response**: `200 OK`: Returns updated goal progress object.

#### Delete Goal
- **Method & Path**: `DELETE /api/goals/{id}`
- **Response**: `200 OK`:
  ```json
  { "message": "Goal deleted successfully" }
  ```

---

### 5. Reports & Financial Analytics

#### Monthly Breakdown Report
- **Method & Path**: `GET /api/reports/monthly/{year}/{month}`
- **Example**: `GET /api/reports/monthly/2024/1`
- **Response**: `200 OK`
  ```json
  {
    "month": 1,
    "year": 2024,
    "totalIncome": {
      "Salary": 3000.00,
      "Freelance": 500.00
    },
    "totalExpenses": {
      "Food": 400.00,
      "Rent": 1200.00,
      "Transportation": 200.00
    },
    "netSavings": 1700.00
  }
  ```
- **Validation**: Month must be in range `1` to `12` (returns `400 Bad Request` if invalid).

#### Yearly Financial Summary
- **Method & Path**: `GET /api/reports/yearly/{year}`
- **Example**: `GET /api/reports/yearly/2024`
- **Response**: `200 OK`
  ```json
  {
    "year": 2024,
    "totalIncome": {
      "Salary": 36000.00,
      "Freelance": 6000.00
    },
    "totalExpenses": {
      "Food": 4800.00,
      "Rent": 14400.00,
      "Transportation": 2400.00
    },
    "netSavings": 20400.00
  }
  ```

---

## Interactive Documentation & Health Metrics

- **Swagger UI**: Access interactive documentation at `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3.0 Spec**: `http://localhost:8080/v3/api-docs`
- **Actuator Health**: `http://localhost:8080/actuator/health`

---

## Build & Local Execution

### Prerequisites
- **JDK 17** installed and active in `PATH`
- **Maven 3.8+** installed

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

### Run Tests
```bash
mvn clean test
```

---

## Render Cloud Deployment Guide

The repository includes a ready-to-use [`render.yaml`](file:///c:/Users/SUSHMA/Desktop/my%20projects/Syfe/render.yaml) blueprint and multi-stage [`Dockerfile`](file:///c:/Users/SUSHMA/Desktop/my%20projects/Syfe/Dockerfile).

### Deployment Environment Configuration
Set the following environment variables in Render:

| Variable | Value | Purpose |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Uses production configuration |
| `COOKIE_SECURE` | `true` | Enforces HTTPS-only secure cookies |
| `PORT` | `8080` | Port binding |

### Validating Live Deployment
Run the official evaluation test script against the deployed endpoint:
```bash
bash financial_manager_tests.sh https://<your-render-app>.onrender.com/api
```
Target output:
```text
================================================
Base URL: https://<your-render-app>.onrender.com/api
Total Tests Executed: 86
Tests Passed: 86
Tests Failed: 0
Success Rate: 100%

🎉 ALL TESTS PASSED! 🎉
The Personal Finance Manager API is working correctly.
================================================
```
