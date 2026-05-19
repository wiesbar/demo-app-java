# demo-app-java

A small Spring Boot 4 + Java 21 service that exposes an arithmetic expression calculator and a health-check endpoint.

## Build & run

Use the Gradle wrapper (`./gradlew` on Unix/Git Bash, `gradlew.bat` on Windows cmd). No Docker daemon is required for any task — the project has no containers, databases, or external services.

```sh
./gradlew build           # compile + run all checks
./gradlew bootRun         # start the service
./gradlew test            # unit tests only (src/test)
./gradlew integrationTest # integration tests only (src/integrationTest)
./gradlew check           # unit + integration tests + Spotless + Checkstyle + JaCoCo verification
./gradlew spotlessApply   # apply google-java-format
./gradlew checkstyleMain  # run Checkstyle over main sources
./gradlew jacocoTestReport # coverage report under build/reports/jacoco/
```

### Dependency management

All plugin and library versions are declared in the Gradle version catalog at `gradle/libs.versions.toml`.
The Spring Boot version is applied via its BOM, imported with Gradle's native `platform(...)` mechanism in `build.gradle.kts`,
so most coordinates inside `dependencies { }` are unversioned. The legacy `io.spring.dependency-management` plugin is not used.
The build script itself stays Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`).

## HTTP API

| Method | Path         | Description                                                |
|--------|--------------|------------------------------------------------------------|
| GET    | `/`          | Health check — returns `"The Demo Service is running!"`.   |
| POST   | `/calculate` | Evaluates the request body as an arithmetic expression.    |

Errors are returned as a JSON body of shape `{ "status", "error", "message" }`. The status mapping is:

- **400 Bad Request** — invalid input from a client (calculator parse error, unparseable request body).
- **500 Internal Server Error** — anything else, including framework-level errors such as using the wrong HTTP method on `/calculate`.

## Profiles

The `calculator` profile registers the `POST /calculate` endpoint and the underlying `ArithmeticExpressionCalculator` bean. It is active by default via `spring.profiles.default=calculator` in `application.yaml`.

`GET /` (health check) is registered on `HealthController` without a profile gate, so it responds regardless of which profiles are active. Requests against unmapped paths fall through to the catch-all `GlobalExceptionHandler` as HTTP 500 (not 404) — matching the framework-error mapping documented above.

## Arithmetic expression calculator

The `/calculate` endpoint is backed by a self-contained expression evaluator in the `example.calculator` package. It supports integer and decimal literals, the binary operators `+`, `-`, `*` (with standard precedence), unary minus, and parentheses for grouping. Invalid input is rejected with a structured error message that points at the offending position.

`POST /calculate` accepts the expression as a plain `text/plain` body and returns the result as a string:

```sh
curl -X POST http://localhost:8080/calculate -H 'Content-Type: text/plain' --data '1 + 2 * 3'
# 7.0
```

See [docs/calculator.md](docs/calculator.md) for the evaluator pipeline, the full syntax reference, examples, and the error catalogue.
