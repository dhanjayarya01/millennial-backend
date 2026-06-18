# Millennial Task Management System — Backend (Core API)

A **Spring Boot 3** REST API serving the Millennial Task Management System. Handles authentication, projects, tasks, work logs, user management, audit logging, and real-time SSE notifications.

---

## Tech Stack

| Layer         | Technology                              |
|---------------|-----------------------------------------|
| Framework     | Spring Boot 3.3 (Web, Security, JPA)    |
| Language      | Java 17                                 |
| Database      | MySQL 8                                 |
| Auth          | JWT (JJWT 0.12)                         |
| ORM           | Hibernate / Spring Data JPA             |
| Build         | Maven (wrapper included)                |
| Docs          | Springdoc OpenAPI 2 (Swagger UI)        |

---

## API Documentation (Swagger UI)

Once the server is running, open **http://localhost:8080/swagger-ui.html** in your browser.

### How to Authenticate in Swagger
1. Call `POST /api/auth/login` → copy the `token` from the response.
2. Click the **Authorize 🔒** button at the top-right of the Swagger page.
3. Enter the token (without the `Bearer ` prefix) and click **Authorize**.
4. All subsequent "Try it out" calls will include the JWT.

### API Groups

| Tag            | Base Path        | Description                                     |
|----------------|------------------|-------------------------------------------------|
| Authentication | `/api/auth`      | Register and login (no JWT required)            |
| Users          | `/api/users`     | Verification, profile picture, password change  |
| Projects       | `/api/projects`  | CRUD + member/manager assignment                |
| Tasks          | `/api/tasks`     | CRUD, status updates, priority changes          |
| Work Logs      | `/api/work-logs` | Log hours with attachments; replies on logs     |
| Audit Logs     | `/api/audit-logs`| System activity history (admin only)            |

---

## Setup Instructions

### Prerequisites
- Java 17+
- MySQL 8 running locally (or remote)
- Maven (or use the included `./mvnw` wrapper)

### Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd millennial-backend
   ```

2. **Create environment file**
   ```bash
   cp .env.example .env
   ```
   Fill in the values:

   | Variable              | Description                          | Example                                 |
   |-----------------------|--------------------------------------|-----------------------------------------|
   | `MYSQLHOST`           | MySQL host                           | `localhost`                             |
   | `MYSQLPORT`           | MySQL port                           | `3307`                                  |
   | `MYSQLDATABASE`       | Database name                        | `millennial_db`                         |
   | `MYSQLUSER`           | Database user                        | `millennial`                            |
   | `MYSQLPASSWORD`       | Database password                    | `your_db_password`                      |
   | `JWT_SECRET`          | 64-char hex string for signing JWTs  | `your_64_char_hex_secret`               |
   | `JWT_EXPIRATION`      | Token TTL in milliseconds            | `86400000` (24 h)                       |
   | `APP_CORS_ORIGINS`    | Comma-separated allowed origins      | `http://localhost:3000`                 |

3. **Run the server**
   ```bash
   ./mvnw spring-boot:run
   ```
   The API is available at **http://localhost:8080**.

4. **Database**
   `spring.jpa.hibernate.ddl-auto=update` will auto-create/update tables on first run.

---

## Architecture Decisions

- **Stateless JWT Auth** — No server-side sessions; each request carries a `Bearer` token validated by `JwtAuthenticationFilter`.
- **Role-based Access** — Three roles: `ROLE_ADMIN`, `ROLE_PROJECT_MANAGER`, `ROLE_EMPLOYEE`. Method-level security via `@EnableMethodSecurity`.
- **SSE Notifications** — `SseEmitter` per user stored in `SseManager`; emitters are removed on timeout or error to avoid memory leaks.
- **Audit Logging** — Every significant action (create/update/delete project, task, work-log) writes an `AuditLog` record automatically in the service layer.
- **Password Security** — `BCryptPasswordEncoder`; current password verified before any password change.

---

## Assumptions

- MySQL is used as the primary data store; schema is managed by Hibernate DDL auto.
- The worker service handles async jobs (email, background notifications) via a Redis queue.
- CORS is configured via `APP_CORS_ORIGINS` to allow only trusted front-end origins.

---

## Available Commands

| Command                      | Description               |
|------------------------------|---------------------------|
| `./mvnw spring-boot:run`     | Run in development mode   |
| `./mvnw clean package`       | Build a fat JAR           |
| `./mvnw test`                | Run unit tests            |
