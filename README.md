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
| Rate Limiting | Bucket4j (token-bucket, per-IP, in-memory) |
| Mapping | MapStruct (compile-time, zero reflection) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| API Docs | OpenAPI 3 / Swagger UI |
| Build | Maven |
| Container | Docker + Docker Compose (multi-stage build) |
| CI/CD | GitHub Actions (CI + auto-deploy to VPS) |

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
│   ├── security/               # JWT service, filter, UserDetailsService, BCrypt adapter, RateLimitFilter
│   ├── config/                 # Spring beans (ApplicationConfig, OpenApiConfig, CorsConfig)
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
- Partial updates: change the original URL, alias, expiry, or toggle a link active/inactive
- Click tracking and analytics per link
- Paginated URL listing
- Access log per redirect (IP, User-Agent, timestamp)
- Per-IP rate limiting on auth and redirect endpoints (Bucket4j)

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | No | Register a new user |
| POST | `/api/v1/auth/login` | No | Login, get JWT token |
| POST | `/api/v1/urls` | Yes | Create a short URL |
| GET | `/api/v1/urls` | Yes | List your URLs (paginated) |
| PATCH | `/api/v1/urls/{id}` | Yes | Partial update (alias, expiry, active toggle) |
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

# 2. Set up environment variables
cp .env.example .env
# Edit .env and fill in your values (DB credentials, JWT secret, etc.)
# Generate a JWT secret with: openssl rand -base64 32

# 3. Start the full stack (app + PostgreSQL)
docker compose up -d

# 4. API is live at http://localhost:8080
# 5. Swagger UI at http://localhost:8080/swagger-ui.html
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

# Partial update — deactivate a link
curl -X PATCH http://localhost:8080/api/v1/urls/<id> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"active":false}'
```

## CI/CD

Two GitHub Actions workflows run automatically:

**CI** — triggers on every push and pull request to `main`:
1. Set up Java 21 (Temurin) with Maven cache
2. Run full test suite (`mvn verify`) including Testcontainers integration tests
3. Upload Surefire test reports as artifacts on failure

**Deploy** — triggers on push to `main` after CI passes:
1. SSH into the VPS
2. Pull latest code
3. Rebuild and restart only the app container (`docker compose up -d --build --no-deps app`) — zero database downtime

## Key Engineering Decisions

**Immutable domain objects** — `ShortUrl.withRecordedClick()` returns a new instance instead of mutating state. Thread-safe by design.

**PasswordEncoderPort** — BCrypt is an infrastructure detail. The domain defines the contract; the adapter wires Spring Security's `BCryptPasswordEncoder`.

**Flyway owns the schema** — Hibernate is set to `ddl-auto: validate`. Schema changes are versioned SQL migrations, not auto-generated DDL.

**Testcontainers over H2** — Integration tests hit a real PostgreSQL container. No dialect mismatches, no hidden incompatibilities.

**MapStruct over manual mapping** — Zero-reflection compile-time mappers between JPA entities and domain objects.

**Bucket4j rate limiting** — Per-IP token buckets protect the two highest-risk endpoints without Redis or external state: auth endpoints (10 req/min) and the redirect endpoint (60 req/min). Fully togglable via `app.rate-limit.enabled`.

**Multi-stage Docker build** — Stage 1 uses the full Maven + JDK image to compile and package. Stage 2 uses a JRE-only Alpine image (~100 MB smaller) and runs as a non-root user for container security.
