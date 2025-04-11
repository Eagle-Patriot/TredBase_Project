# TredBase Payment System

A Spring Boot application demonstrating **secure payment processing** for a family-oriented service. Parents can pay for children who may be uniquely associated or shared between two parents. The payment logic ensures atomicity and logs transactions (both successes and failures).

---

## Table of Contents

1. [Overview](#overview)
2. [Key Features](#key-features)
3. [Project Structure](#project-structure)
4. [Application Properties](#application-properties)
5. [Building & Running](#building--running)
6. [Endpoints & Usage](#endpoints--usage)
7. [Authentication & Security](#authentication--security)
8. [Multi-Table Payment Processing](#multi-table-payment-processing)
9. [Arithmetic Logic & Balance Updates](#arithmetic-logic--balance-updates)
10. [Integration Tests](#integration-tests)
11. [Design Decisions](#design-decisions)

---

## 1. Overview

This system manages financial transactions where parents make payments for children. A child can be:

- **Unique** to one parent
- **Shared** between two parents

When a payment is made for a **shared** child, costs are split across both parents. If **any** step fails (e.g., insufficient balance), it rolls back changes but still record a **FAILED** Payment entry for auditing.

---

## 2. Key Features

- **Spring Security** with Basic Auth – only admins can process or view payments.
- **H2 In-Memory Database** – easy testing.
- **Transactional** multi-table updates for consistency.
- **5% Fee** on each payment.
- **Integration Tests** covering all scenarios.

---

## 3. Project Structure

```
├── src
│   └── main
│       ├── java
│       │   └── com.example.Tredbase_payment_system
│       │       ├── Config          (WebSecurityConfig)
│       │       ├── Controller     (REST endpoints)
│       │       ├── Dto            (PaymentRequest DTO)
│       │       ├── Entity         (Parent, Student, Payment)
│       │       ├── Repository     (JPA repositories)
│       │       ├── Service        (PaymentService, PaymentLogService)
│       │       └── Utils          (TransactionStatus enum)
│       └── resources
│           ├── application.properties
│           └── import.sql          (Seeds initial data for Parents/Students)
└── ...
```

---

## 4. Application Properties

```properties
spring.application.name=Tredbase_payment_system

# H2 In-Memory Database Config
spring.datasource.url=jdbc:h2:mem:payments_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# On startup, create the schema from the entities
spring.jpa.hibernate.ddl-auto=create

# Dialect for H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Enable H2 console for debugging
spring.h2.console.enabled=true
spring.jpa.show-sql=true

# Security Debug Logging
logging.level.org.springframework.security=DEBUG
```

### Explanation

- **jdbc:h2:mem:payments_db**: In-memory DB named `payments_db`.
- **ddl-auto=create**: Re-creates tables on each startup (data is temporary).
- **h2-console.enabled**: Allows you to visit `http://localhost:8080/h2-console` to inspect the DB.

---

## 5. Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/youruser/tredbase-payment-system.git
   cd tredbase-payment-system
   ```

2. **Build & Run** (Maven):
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
    - The app starts on **port 8080** at `http://localhost:8080` as default.

3. **Database & H2 Console**:
    - Access the console at: `http://localhost:8080/h2-console`
    - Use the JDBC URL: `jdbc:h2:mem:payments_db`, username: `sa`, password: *(blank)*

4. **Authentication**:
    - **Username**: `admin`
    - **Password**: `adminPass`

---

## 6. Endpoints & Usage

**All** endpoints require Basic Auth (`ROLE_ADMIN`).

1. **Welcome**
    - `GET /`
    - Returns a simple message: “Payment Service is running”

2. **Process Payment**
    - `POST /api/payment`
    - **Body (JSON)**:
      ```json
      {
        "parentId": 1,
        "studentId": 2,
        "paymentAmount": 100.0
      }
      ```
    - If successful, returns **HTTP 200** with "Payment processed successfully".
    - If an error occurs (e.g., insufficient balance, or parent not associated), returns **HTTP 400** with “Payment failed: {reason}”.

3. **Get Students**
    - `GET /students`
    - Returns an array of all students, including their balances.

4. **Get Payments**
    - `GET /payments`
    - Returns an array of all Payment records, with success/failure status and a description.


---

## 7. Authentication & Security

- **WebSecurityConfig**
    - Disables CSRF for a stateless API.
    - Restricts `POST /api/payment` and other read endpoints (`/students`, `/payments`) to users with `ROLE_ADMIN`.
- **Basic Auth**
    - The admin user is defined in an in-memory store with a **BCrypt** password.

```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails adminUser = User
        .withUsername("admin")
        .password(passwordEncoder().encode("adminPass"))
        .roles("ADMIN")
        .build();

    return new InMemoryUserDetailsManager(adminUser);
}
```

---

## 8. Multi-Table Payment Processing

1. **Parent** table:
    - `id`, `name`, `balance`.

2. **Student** table:
    - `studentId`, `studentName`, `balance`.
    - Many-to-many with `Parent` via `parent_student` join table.

3. **Payment** table:
    - Logs **each** transaction attempt.
    - Fields: `id`, `parentId`, `studentId`, `amount`, `status`, `paymentDate`, and an optional `description`.

### Transaction Flow

- **PaymentService.processPayment** is annotated with `@Transactional`.
- If any check fails (insufficient funds, mismatch parent/student), an exception is thrown, triggering a **rollback**.
- **However**, a `PaymentLogService` with `@Transactional(propagation = REQUIRES_NEW)` ensures that the **FAILED** Payment record is always persisted, even if the main transaction is rolled back.

---

## 9. Arithmetic Logic & Balance Updates

A **dynamicRate** of 5% is applied to each payment:

```
adjustedAmount = paymentAmount * (1 + 0.05) = paymentAmount * 1.05
```

- **Unique Student**:
    - Only one parent is billed for `adjustedAmount`.
- **Shared Student**:
    - Both parents pay **half** of `adjustedAmount`. If either parent is underfunded, the entire transaction fails.

**Student’s balance** always increases by the base `paymentAmount` (not the adjusted). The extra 5% is effectively a fee that parents cover.

---

## 10. Integration Tests

The class **`PaymentServiceIntegrationTest`** verifies:

- **Unique Student** success (correct parent balance deduction).
- **Shared Student** success (split cost).
- **Insufficient Balance** failure (rolls back balance changes, logs a FAILED payment).
- **Parent Not Associated** failure.

```java
@SpringBootTest
class PaymentServiceIntegrationTest {

    @Test
    void testProcessPayment_UniqueStudent_Success() {  }

    @Test
    void testFail_NotAssociated() { }

    // ...
}
```

Each test checks final parent/student balances and the Payment record’s **status** and **description** to confirm correct behavior.

---

## 11. Design Decisions

1. **Atomic Transactions**:
    - Ensures partial updates (like one parent’s balance deducting) are never committed if something else fails.

2. **Security Approach**:
    - Basic Auth is simple for internal services.
    - Only `ROLE_ADMIN` can make or view payments.

3. **Dynamic Fees**:
    - The 5% fee is a demonstration of “non-trivial arithmetic.”
    - Could be replaced with environment variables or advanced logic later.

4. **import.sql** for Initialization:
    - Seeds two parents (A, B) and three students:
        - Student 1: shared by both
        - Student 2: unique to A
        - Student 3: unique to B

5. **PaymentLogService**:
    - Uses a separate transaction so that **FAILED** Payment records remain in the DB even if the main transaction is rolled back.