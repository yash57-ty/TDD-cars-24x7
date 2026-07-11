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

* **Antigravity (built by Google DeepMind)**: Used as a development assistant for testing, debugging, and occasional frontend implementation support.

### 🧩 How AI Was Leveraged

1. **Test-Driven Development (TDD)**: Primarily used AI to generate and refine failing (RED) unit and integration tests for features such as authentication, vehicle CRUD operations, and search functionality.
2. **Testing & Debugging Support**: Used AI to analyze test failures, suggest debugging approaches, and explain testing best practices during development.
3. **Occasional Frontend Assistance**: Used AI to brainstorm UI layouts, improve React component structure, and resolve frontend issues when needed. The final implementation and customization were completed by me.
4. **Documentation & Code Review**: Occasionally used AI to review code, improve documentation, and suggest minor refactorings without changing the overall application design.

### 💻 Development Ownership

* I independently designed and implemented the **backend, REST APIs, database schema, business logic, authentication, and core application architecture**.
* I also developed the frontend myself, using AI only occasionally for UI suggestions or resolving specific implementation issues.
* All major technical decisions, feature implementations, and final code were written, verified, and maintained by me.

### 💡 Workflow Reflections

* Using AI primarily for testing and selective development assistance improved productivity while allowing me to retain full ownership of the application's architecture and implementation.
* Following a Test-Driven Development (TDD) approach helped improve code quality, with AI serving as a supporting tool rather than replacing the development process.
