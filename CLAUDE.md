# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Spring Boot 4.0.5 + Java 21, built with Gradle (Kotlin DSL — `build.gradle.kts` / `settings.gradle.kts`).
All plugin and library versions are declared in the Gradle version catalog at `gradle/libs.versions.toml`;
Spring Boot is applied as a BOM via Gradle's native `platform(...)` mechanism (no `io.spring.dependency-management` plugin).
Uses Spring Boot's `spring-boot-starter-webmvc` (servlet MVC).

No Docker is required for this project — there are no containers, databases, or external services. Every Gradle task, including `integrationTest` and `check`, runs on a bare JVM.

## Commands

Use the Gradle wrapper. On Windows bash, `./gradlew` works; `gradlew.bat` is also available.

- Build: `./gradlew build`
- Run app: `./gradlew bootRun` (starts on the default port 8080)
- Run unit tests: `./gradlew test` (only `src/test`)
- Run integration tests: `./gradlew integrationTest` (only `src/integrationTest`)
- Run a single test class: `./gradlew test --tests "example.calculator.ArithmeticExpressionCalculatorTest"` (use the `integrationTest` task for IT classes)
- Format: `./gradlew spotlessApply` (google-java-format); check only with `./gradlew spotlessCheck`
- Static analysis: `./gradlew checkstyleMain` / `./gradlew checkstyleTest`
- Coverage: `./gradlew jacocoTestReport` (HTML under `build/reports/jacoco/test/html/`) or `./gradlew jacocoTestCoverageVerification` (fails below 90% line coverage)
- `./gradlew check` runs unit tests + integration tests + Spotless + Checkstyle + JaCoCo verification together. Run `./gradlew spotlessApply check` so formatting fixes are applied before verification.

## Architecture

Spring Boot web service split across packages under root package `example`:

- `example` — `DemoApplication` (the `@SpringBootApplication` entry point in `DemoApplication.java`). The class is intentionally bare — no `@Bean` factories — and all wiring is delegated to the profile-gated configuration in `example.config`.
- `example.config` — profile-gated Spring `@Configuration` classes. `CalculatorConfig` (`@Profile("calculator")`) wires `calculator()` → `DefaultArithmeticExpressionCalculator`. Keeping the wiring here means `example.calculator` stays free of Spring annotations.
- `example.web` — HTTP surface.
  - `HealthController` — `GET /` returns the static string `"The Demo Service is running!"`. No profile gate; this endpoint is always available.
  - `CalculatorController` — `@Profile("calculator")`. `POST /calculate` runs the request body through the injected `ArithmeticExpressionCalculator` and returns the result as a string (e.g. `"7.0"`). The constructor is package-private so the public class doesn't widen the visibility of the calculator port; Spring still resolves it via reflection.
  - `GlobalExceptionHandler` — `@RestControllerAdvice` with handlers ordered most-specific first: `InvalidArithmeticExpressionException` → HTTP 400; `HttpMessageNotReadableException` → HTTP 400 (uses `getMostSpecificCause().getMessage()`); any other `Exception` → HTTP 500 with body `{status: "500", error: "Internal Server Error", message: "Internal Server Error"}`. The 500 message is static and never leaks the cause. Because the catch-all matches `Exception`, framework errors like `HttpRequestMethodNotSupportedException` (e.g. `GET /calculate`) are returned as 500 — see `UnsupportedOperationsTest`. The 400 handler methods are package-private so the public class doesn't expose the internal exception type. If you add more specific handlers, order matters — Spring picks the most specific.
- `example.calculator` — pure (non-Spring) arithmetic engine. Most types are package-private; only `ArithmeticExpressionCalculator`, `DefaultArithmeticExpressionCalculator`, and `InvalidArithmeticExpressionException` are `public`, because they are referenced from `example.config` / `example.web` (Java has no module-level `internal` visibility, so the cross-package entry points must be public while the parsing pipeline stays package-private). Pipeline: `InfixParser.toInfix(expression)` (tokenizer) → `PostfixParser.toPostfix(infix)` (shunting-yard) → `Postfix.calculate(postfix)` (stack evaluator). `Tokens.java` defines the sealed `Token` hierarchy as Java sealed interfaces + records (`Operand`, `BinaryOperator.{Plus,Minus,Times}`, `UnaryOperator.Negate`, `Parenthesis.{Left,Right}`); each token carries its `positionInExpression` so error messages can point at the offending character. `Result.java` models the evaluator's intermediate results (`Value`, `Sum`, `Difference`, `Product`, `Negation`) the same way, each with a secondary constructor that derives value/position from its inputs. `ArithmeticExpressionCalculator` is a `@FunctionalInterface`, with `DefaultArithmeticExpressionCalculator` as the production implementation. All parse/evaluate errors throw the engine's own `InvalidArithmeticExpressionException` (a plain `RuntimeException`) — do not use `Objects.requireNonNull` / `IllegalArgumentException` for engine validation, since that breaks the domain-specific exception contract `GlobalExceptionHandler` relies on for the 400 mapping. Supported syntax: integers/decimals (including leading `.`), whitespace, `+`, `-` (binary and unary), `*`, parentheses. Unary `+` is rejected. There is no `/` operator yet.

`application.yaml` sets `spring.application.name=demo-application`, `spring.profiles.default=calculator`, and disables the Spring whitelabel error page so `GlobalExceptionHandler` is the sole error renderer.

## Code style rules

- Function bodies should not exceed 15 lines.
- Formatting is owned by Spotless (`googleJavaFormat()`). Run `./gradlew spotlessApply` before committing.

## Testing conventions

- **JUnit 5 + AssertJ.** Use AssertJ (`assertThat`, `assertThatThrownBy`, etc.) for assertions.
- **Source set split**: unit tests live in `src/test/java` and run under `./gradlew test`; integration tests live in `src/integrationTest/java` and run under `./gradlew integrationTest`. Both are wired into `./gradlew check`. The `integrationTest` source set extends `testImplementation`/`testRuntimeOnly`, so the Spring Boot test starters and `RestTestClient` are available without re-declaring them.
- **Naming**: both unit and integration test classes use the `*Test` suffix. Disambiguate by location (source set), not by suffix. One class per HTTP endpoint or behavioral concern (e.g. `DemoServiceTest`, `CalculatorEndpointTest`, `UnsupportedOperationsTest`) rather than one per controller — this keeps each test narrowly focused and lets Spring's test context cache reuse the same `ApplicationContext`.
- Integration tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureRestTestClient` + `@ActiveProfiles("calculator")` on the class, with a `RestTestClient` injected via `@Autowired` field injection. HTTP assertions go through `RestTestClient` (Spring Boot 4's test client).
- Use JUnit 5 `@ParameterizedTest` + `@MethodSource` for table-driven tests. See `ArithmeticExpressionCalculatorTest` for the pattern (it uses `@MethodSource` for both the valid-expression and invalid-expression tables).
- Error-path tests in `ArithmeticExpressionCalculatorTest` assert the full exception message string verbatim, so when you change a message you must update its test row in lockstep.

## TDD Rules

- Always use strict TDD (Red-Green-Refactor) for code changes.
- Write a failing test first.
- Implement only what is necessary to pass the test.
- Refactor after passing.
