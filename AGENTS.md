# Repository Guidelines
Pick Perfume Backend is a Kotlin/Spring Boot service providing perfume recommendations and supporting admin workflows.

## Project Structure & Module Organization
Source lives under `src/main/kotlin/ym_cosmetic/`, organized by bounded context (`auth`, `perfume`, `recommendation`, `search`, etc.). Place DTOs and REST controllers under the same feature package to keep flows readable. Shared utilities belong in `common/`, while infrastructure adapters (S3, OpenSearch, mail) stay in `infrastructure/`. Configuration and SQL resources sit in `src/main/resources/` (`application.yml`, `db/`, `analysis/`). Tests mirror the production tree in `src/test/kotlin/ym_cosmetic/`. Data assets, notebooks, or one-off scripts belong in `docs/`, `csv/`, or `perfume-importer/`; infrastructure code lives in `terraform/`.

## Build, Test, and Development Commands
Use `./gradlew bootRun` for the API with local profiles (`SPRING_PROFILES_ACTIVE=local`). Run `docker compose -f docker-compose.local.yml up -d` to provision MySQL and OpenSearch dependencies. Validate builds with `./gradlew clean build`, and execute targeted suites via `./gradlew test --tests "ym_cosmetic.*"` when iterating. Regenerate QueryDSL stubs by cleaning the build directory if package shapes change.

## Coding Style & Naming Conventions
Follow the official Kotlin style: four-space indents, trailing commas disabled, and explicit visibility on non-local declarations. Classes and interfaces use UpperCamelCase, functions and properties use lowerCamelCase, and request/response DTOs end with `Request` or `Response`. Keep package names lowercase and align controller method names with HTTP verbs (`create`, `update`, `list`). Avoid editing generated sources in `build/`; treat them as build artifacts.

## Testing Guidelines
Spring/JUnit 5 is the default test runner with Kotest assertions and MockK for doubles. Name files `*Test.kt` for integration suites and `*Spec.kt` for behaviour-driven Kotest specs. Use `@SpringBootTest` sparingly; prefer sliced tests (`@DataJpaTest`, `@WebMvcTest`) to keep feedback fast. Run the full suite (`./gradlew test`) before submitting and capture new scenarios with Kotest data-driven blocks where possible.

## Commit & Pull Request Guidelines
Write commits in the imperative and prefer conventional prefixes (`feat:`, `fix:`, `chore:`) as seen in recent history. Group refactors separately from functional changes. PRs should reference the related issue, summarise domain impact, list new environment variables, and attach screenshots or sample payloads for API-facing updates. Include validation notes (tests, migrations, manual checks) so reviewers can reproduce your result quickly.
