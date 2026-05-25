# Spring Security Passkey

A Spring Boot demo application showcasing **passkey (WebAuthn)** authentication combined with **multi-factor authentication (MFA)** and **one-time token (OTT)** login, using Spring Security 7.x.

---

## Features

- **WebAuthn / Passkey login** — register and authenticate using platform authenticators (Face ID, Touch ID, Windows Hello, etc.)
- **Multi-Factor Authentication** — enforces both password and OTT factors via `@EnableMultiFactorAuthentication`
- **One-Time Token (OTT) login** — magic-link style login delivered via console (useful for development/testing)
- **In-memory user store** — two pre-configured users (`user` / `admin`) for quick local testing

---

## Tech Stack

| Layer | Technology                                   |
|---|----------------------------------------------|
| Language | Java 25                                      |
| Framework | Spring Boot 4.*                              |
| Security | Spring Security + `spring-security-webauthn` |
| Web | Spring MVC                                   |
| Build | Maven                                        |

---

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+
- A browser that supports WebAuthn (Chrome, Safari, Firefox, Edge)

### Run the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## Usage

### 1. Register a Passkey

Navigate to the WebAuthn registration endpoint:

```
http://localhost:8080/webauthn/register
```

Follow the browser prompts to register a platform authenticator (biometric, security key, etc.).

### 2. Log In with a Passkey

After registration, use the standard login page — the browser will offer passkey authentication automatically.

### 3. One-Time Token Login

Request a magic link by submitting your username. The token URL is printed to the console:

```
http://localhost:8080/login/ott?token=<token>
```

Open the URL in your browser to complete passwordless login.

### 4. Verify Authentication

Once logged in, call the root endpoint to confirm your identity:

```
GET http://localhost:8080/
```

**Response:**
```json
{ "name": "user" }
```

---

## Pre-configured Users

| Username | Password | Roles |
|---|---|---|
| `user` | `user` | USER |
| `admin` | `admin` | ADMIN, USER |

> **Note:** Passwords are stored in plain text (`{noop}`) for development convenience only. Do not use this in production.

---

## Configuration

### Security (`SecurityConfig.java`)

- **WebAuthn** is configured with `rpId: localhost` and `rpName: kgromov` for local development.
- **MFA** requires both `PASSWORD_AUTHORITY` and `OTT_AUTHORITY` to be satisfied.
- **OTT handler** writes the token URL to stdout and returns a plain-text response to the browser.

### Application properties (`application.yaml`)

```yaml
spring:
  application:
    name: spring-security-passkey
```

---

## Project Structure

```
src/
├── main/
│   ├── java/org/kgromov/
│   │   ├── SpringSecurityPasskeyApplication.java   # Entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java                 # Security configuration
│   │   └── controller/
│   │       └── UsersController.java                # REST endpoints
│   └── resources/
│       └── application.yaml
└── test/
    └── java/org/kgromov/
        └── SpringSecurityPasskeyApplicationTests.java
```

---

## License

This project is provided as a demonstration/learning resource and carries no specific license.