# URL Shortener API

A production-style URL shortener REST API built as a backend portfolio project.  
Demonstrates clean architecture, professional Java engineering, and modern DevOps practices.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Architecture | Hexagonal (Ports & Adapters) |
| Database | PostgreSQL 16 + Flyway migrations |
| Security | Spring Security + JWT (JJWT 0.12) |
| Mapping | MapStruct (compile-time, zero reflection) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| API Docs | OpenAPI 3 / Swagger UI |
| Build | Maven |
| Container | Docker + Docker Compose |
| CI | GitHub Actions |

## Architecture

This project follows **Hexagonal Architecture (Ports & Adapters)** — the domain has zero framework dependencies.

```
src/main/java/com/urlshortener/
│
├── domain/                     # Pure Java — no Spring, no JPA
│   ├── model/                  # Business objects (User, ShortUrl, AccessLog)
│   ├── port/
│   │   ├── in/                 # Input ports: what the app can DO (use case interfaces)
│   │   └── out/                # Output ports: what the app NEEDS (repository interfaces)
│   └── exception/              # Domain exceptions
│
├── application/
│   └── service/                # Use case implementations — orchestrate domain + ports
│
├── infrastructure/
│   ├── persistence/            # JPA entities, Spring Data repos, adapters, MapStruct mappers
│   ├── security/               # JWT service, filter, UserDetailsService, BCrypt adapter
│   ├── config/                 # Spring beans (ApplicationConfig, OpenApiConfig)
│   └── adapter/                # ShortCodeGenerator (Base62)
│
└── adapter/
    └── in/web/                 # REST controllers, DTOs, GlobalExceptionHandler
```

**Why hexagonal?**
- Business logic is completely isolated — you can swap PostgreSQL for MongoDB without touching a single use case
- Every layer is independently testable
- Controllers depend on interfaces, never on concrete services

## Features

- User registration and login with JWT authentication
- Shorten any URL with an auto-generated 7-character Base62 code
- Optional custom aliases (e.g. `/google` instead of `/xK3mN9p`)
- Optional expiration dates
- Click tracking and analytics per link
- Paginated URL listing
- Access log per redirect (IP, User-Agent, timestamp)

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | No | Register a new user |
| POST | `/api/v1/auth/login` | No | Login, get JWT token |
| POST | `/api/v1/urls` | Yes | Create a short URL |
| GET | `/api/v1/urls` | Yes | List your URLs (paginated) |
| GET | `/api/v1/urls/{id}/analytics` | Yes | Get click analytics |
| DELETE | `/api/v1/urls/{id}` | Yes | Delete a URL |
| GET | `/{code}` | No | Redirect to original URL |

Full interactive documentation available at `/swagger-ui.html`.

## Running Locally

**Prerequisites:** Docker, Java 21, Maven

```bash
# 1. Clone the repo
git clone git@github.com:LucasMartinez99/url-shortener.git
cd url-shortener

# 2. Start the full stack (app + PostgreSQL)
docker compose up -d

# 3. API is live at http://localhost:8080
# 4. Swagger UI at http://localhost:8080/swagger-ui.html
```

To run just the database and use Maven for hot-reload during development:

```bash
docker compose up -d db
mvn spring-boot:run
```

## Running Tests

```bash
# Unit tests only (no Docker required — fast)
mvn test -Dtest="ShortUrlServiceTest,UserServiceTest"

# Integration tests (Testcontainers pulls PostgreSQL automatically)
mvn test -Dtest="AuthControllerTest,ShortUrlControllerTest"

# Full test suite
mvn test
```

Integration tests spin up a real PostgreSQL container via Testcontainers — no mocks, no surprises.

## Quick API Demo

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"password123"}'

# Create a short URL
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://github.com/LucasMartinez99/url-shortener","customAlias":"my-project"}'

# Redirect
curl -L http://localhost:8080/my-project
```

## CI/CD

GitHub Actions runs on every push to `main` and `dev`:
1. Compile
2. Run all tests (Testcontainers works on `ubuntu-latest`)
3. Package JAR and upload as artifact

## Key Engineering Decisions

**Immutable domain objects** — `ShortUrl.withRecordedClick()` returns a new instance instead of mutating state. Thread-safe by design.

**PasswordEncoderPort** — BCrypt is an infrastructure detail. The domain defines the contract; the adapter wires Spring Security's `BCryptPasswordEncoder`.

**Flyway owns the schema** — Hibernate is set to `ddl-auto: validate`. Schema changes are versioned SQL migrations, not auto-generated DDL.

**Testcontainers over H2** — Integration tests hit a real PostgreSQL container. No dialect mismatches, no hidden incompatibilities.

**MapStruct over manual mapping** — Zero-reflection compile-time mappers between JPA entities and domain objects.
