# Spring Security Passkey

A Spring Boot demo application showcasing **passkey (WebAuthn)** authentication combined with **multi-factor authentication (MFA)** and **one-time token (OTT)** login, using Spring Security 7.x.

---

## Features

- **WebAuthn / Passkey login** — register and authenticate using platform authenticators (Face ID, Touch ID, Windows Hello, etc.)
- **Multi-Factor Authentication** — enforces password + WebAuthn factors via `@EnableMultiFactorAuthentication`
- **One-Time Token (OTT) login** — PIN-based or magic-link style login delivered via console
- **Dual storage profiles** — in-memory (default) or JDBC-backed (PostgreSQL) user/credential store
- **Docker Compose integration** — PostgreSQL + PostgREST + Swagger UI via `spring-boot-docker-compose`
- **Caffeine-cached PIN tokens** — custom `PinOneTimeTokenService` with configurable TTL

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.* |
| Security | Spring Security 7 + `spring-security-webauthn` |
| Web | Spring MVC |
| Persistence | Spring Data JDBC + PostgreSQL |
| Cache | Caffeine |
| Build | Maven |
| Infrastructure | Docker Compose (PostgreSQL, PostgREST, Swagger UI) |

---

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+
- Docker & Docker Compose (for the `jdbc` profile)
- A browser that supports WebAuthn (Chrome, Safari, Firefox, Edge)

### Run — in-memory mode (default)

No database required. Users and credentials are held in memory.

```bash
mvn spring-boot:run
```

### Run — JDBC mode (PostgreSQL)

Uses the bundled `docker-compose.yml` (started automatically by `spring-boot-docker-compose`).

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=jdbc
```

The application starts on `http://localhost:8080`.  
PostgREST API is available at `http://localhost:3000`.  
Swagger UI is available at `http://localhost:8081`.

---

## Profiles

| Profile | User store | OTT service | WebAuthn credentials |
|---|---|---|---|
| `default` / `inMemory` | `InMemoryUserDetailsManager` | `PinOneTimeTokenService` (Caffeine) | In-memory |
| `jdbc` | `JdbcUserDetailsManager` | `JdbcOneTimeTokenService` | PostgreSQL |
| `filter-chain` | _(combined with above)_ | Explicit `SecurityFilterChain` bean | Same as above |

---

## Configuration

### Application properties (`application.yaml`)

```yaml
spring:
  sql:
    init:
      mode: always   # auto-runs schema.sql on startup

otp:
  duration: 1m       # OTT / PIN token TTL (default: 3m)

logging:
  level:
    org.springframework.security: TRACE
```

### Security (`SecurityConfig.java`)

- **MFA** requires `PASSWORD_AUTHORITY` **and** `WEBAUTHN_AUTHORITY` to be satisfied.
- **WebAuthn** is configured with `rpId: localhost` and `rpName: kgromov` for local development.
- **OTT handler** prints the magic link to stdout, writes a plain-text acknowledgement to the response, then redirects to `/ott/sent`.
- A `filter-chain` profile exposes a traditional `SecurityFilterChain` bean instead of the `Customizer<HttpSecurity>` approach.

### Docker Compose (`.env`)

```
POSTGRES_DB=mydatabase
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

PostgREST runs with `PGRST_DB_ANON_ROLE=postgres` for anonymous read access during development.

---

## Usage

### 1. Register a Passkey

```
http://localhost:8080/webauthn/register
```

Follow the browser prompts to register a platform authenticator (biometric, hardware key, etc.).

### 2. Log In with a Passkey

Use the standard login page — the browser will offer passkey authentication once a credential is registered.

### 3. One-Time Token Login

Submit your username on the OTT login page. The token/PIN is printed to the console:

```
please go to http://localhost:8080/login/ott?token=<token>
```

Open the URL in your browser to complete passwordless login.  
The PIN expires after the configured `otp.duration` (default: 1 minute).

### 4. Verify Authentication

```
GET http://localhost:8080/
```

**Response:**
```json
{ "name": "user" }
```

---

## Pre-configured Users

Populated on startup (both profiles).

| Username | Password | Roles |
|---|---|---|
| `user` | `user` | USER |
| `admin` | `admin` | ADMIN, USER |

> **Note:** `withDefaultPasswordEncoder()` is deprecated and used here for convenience only. Do not use in production.

---

## Database Schema

Managed by `schema.sql` (auto-applied via `spring.sql.init.mode=always`).

| Table | Purpose |
|---|---|
| `users` | Spring Security user accounts |
| `authorities` | Granted authorities / roles |
| `user_entities` | WebAuthn user entity records |
| `user_credentials` | WebAuthn public key credentials |
| `one_time_tokens` | JDBC-backed OTT storage |

---

## Project Structure

```
src/
├── main/
│   ├── java/org/kgromov/
│   │   ├── SpringSecurityPasskeyApplication.java   # Entry point
│   │   ├── config/
│   │   │   ├── SecurityConfig.java                 # MFA, WebAuthn, OTT setup
│   │   │   ├── InMemorySecurityConfig.java          # Default/inMemory profile beans
│   │   │   ├── JdbcSecurityConfig.java              # jdbc profile beans
│   │   │   ├── PinOneTimeTokenService.java          # Caffeine-backed PIN OTT service
│   │   │   └── UsersPopulator.java                  # Seeds users on jdbc profile startup
│   │   └── controller/
│   │       └── UsersController.java                # GET / → current user
│   └── resources/
│       ├── application.yaml
│       └── schema.sql                              # DDL for all security tables
├── test/
│   └── java/org/kgromov/
│       └── SpringSecurityPasskeyApplicationTests.java
└── docker-compose.yml                              # PostgreSQL + PostgREST + Swagger UI
```

---

## License

This project is provided as a demonstration/learning resource and carries no specific license.