# 🚗 Car Dealership Inventory Management System (TDD-cars-24x7)

A robust, production-ready, full-stack Car Dealership Inventory Management System built with **Spring Boot 3.3.2**, **React**, **PostgreSQL**, **Spring Security (JWT)**, and developed strictly using **Test-Driven Development (TDD)**.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Java 21 + Spring Boot 3.3.2 |
| **Database** | PostgreSQL 16+ |
| **Security / Auth** | Spring Security + JWT (JSON Web Token) |
| **Database Access** | Spring Data JPA + Hibernate 6 |
| **Testing** | JUnit 5 + Mockito + MockMvc + H2 (in-memory test DB) |
| **Performance** | HikariCP Connection Pool |

---

## 🏗️ Architecture & Scalability Highlights

This system is built to be highly robust and scalable, engineered to survive concurrent production loads without data corruption:

### 1. Concurrency Control (Pessimistic Locking)
During high concurrent user volumes (e.g., multiple users checking out the same vehicle at once), standard database transactions can suffer from **Lost Update** race conditions. 
We implemented row-level locking (`SELECT ... FOR UPDATE`) in `VehicleRepository` using JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)`. This locks the vehicle row until the transaction commits, ensuring that stock decrements are strictly serialized and preventing over-selling.

### 2. High-Performance Connection Pooling (HikariCP)
Configured with HikariCP settings in `application.properties` to ensure efficient database connection lifecycle management under load:
* **Max Pool Size:** 20 concurrent connections.
* **Idle Timeout:** 5 minutes.
* **Leak Detection Threshold:** 15 seconds to identify slow-running queries.

### 3. OWASP Security Compliance
Configured standard HTTP security response headers in `SecurityConfig.java` to prevent injection attacks and clickjacking:
* Frame Options set to `DENY`.
* Content Security Policy (CSP) set to `'self'`.

---

## 📋 REST API Specification

### 🔐 Authentication (`/api/auth`)
* **`POST /api/auth/register`** — Register a new user profile.
  * Request Body: `{ "name": "...", "email": "...", "password": "..." }`
  * Response: `201 Created` with JWT token.
* **`POST /api/auth/login`** — Authenticate and receive token.
  * Request Body: `{ "email": "...", "password": "..." }`
  * Response: `200 OK` with JWT token.

### 🚗 Vehicle CRUD (`/api/vehicles`)
* **`POST /api/vehicles`** — Create a new vehicle *(Authenticated Users)*.
  * Request Body: `{ "make": "...", "model": "...", "category": "...", "price": 0.00, "quantity": 0 }`
* **`GET /api/vehicles`** — Retrieve all vehicles.
* **`GET /api/vehicles/{id}`** — Retrieve specific vehicle details.
* **`PUT /api/vehicles/{id}`** — Update vehicle specifications.
* **`DELETE /api/vehicles/{id}`** — Delete a vehicle *(Admin Users Only)*.
  * Response: `204 No Content`.

### 🔍 Vehicle Search
* **`GET /api/vehicles/search`** — Filter vehicles dynamically in the database via query parameters.
  * Parameters (Optional): `make`, `model`, `category`, `minPrice`, `maxPrice`.

### 📦 Inventory Operations
* **`POST /api/vehicles/{id}/purchase`** — Purchase one unit of a vehicle *(Authenticated Users)*.
  * Decrements stock quantity by 1. Returns `400 Bad Request` if stock is `0`.
* **`POST /api/vehicles/{id}/restock`** — Restock vehicle inventory *(Admin Users Only)*.
  * Request Body: `{ "amount": 10 }`

---

## 🚀 Local Installation & Running Guide

### 1. Database Configuration
Make sure PostgreSQL is running and create the database:
```sql
CREATE DATABASE "TDD-Cars";
```
Verify your credentials in `Backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/TDD-Cars
spring.datasource.username=postgres
spring.datasource.password=yash@2006
```

### 2. Start the Backend
```bash
cd Backend
.\mvnw.cmd spring-boot:run
```
The API server runs on **`http://localhost:8080`**.

### 3. Run Automated Tests
H2 database handles testing. Run tests:
```bash
.\mvnw.cmd test
```

---

## 🤖 My AI Usage

### 🛠️ AI Tools Used
* **Antigravity (built by Google DeepMind)**: Leveraged for codebase discovery, step-by-step TDD test generation, database performance refactoring, and troubleshooting SQL grammar mismatches.

### 🧩 How AI Was Leveraged
1. **API & Package Brainstorming**: Used the AI to design the Restful endpoints layout, DTO requirements, and packaging patterns aligned with standard Spring Boot architectures.
2. **Strict Test-Driven Development (TDD) Loop**: Used the assistant to draft RED (failing) tests first for authentication (`AuthServiceTest`, `AuthControllerIntegrationTest`), vehicle CRUD, and search functionality before writing the actual code.
3. **Concurrency Analysis & Debugging**:
   * Evaluated database transaction locks when the multithreaded `ConcurrentPurchaseIntegrationTest` failed.
   * Leveraged AI to implement row-level pessimistic locking (`SELECT ... FOR UPDATE`) in `VehicleRepository` to resolve the lost update race condition.
   * Debugged the H2 SQL syntax error by overriding the PostgreSQL dialect inherited from main config properties inside `application-test.properties`.
4. **Boilerplate & Spring Configurations**: Generated JWT encoding/decoding stubs, security request filtering, and exception mapper configurations.

### 💡 Workflow Reflections
* **Velocity & Context Isolation**: Writing failing tests first (RED) and then generating implementation code (GREEN) kept the codebase tightly scope-restricted. Having an AI write tests first helped identify edge cases (like out-of-stock scenarios or unauthorized route access) before coding began.
* **Complex Bug Resolution**: AI was highly effective in explaining why concurrent transactions failed (the expected 3 vs actual 10 successes due to default isolation levels) and mapping it to a database pessimistic lock. This transformed what would have been a manual debugging cycle into an immediate database architectural fix.
* **Responsible Integration**: Ensuring clear separation between test design and business implementation enforced software quality, with the final code verification remaining developer-driven.

