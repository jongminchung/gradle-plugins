# gradle-plugins

Opinionated Gradle convention plugins and a Spring Boot application plugin that encode the build defaults I reach for in
most JVM services. Today the repo focuses on those conventions plus the aggregator plugin, and in the near future it
will grow additional Gradle plugins tailored to specific project types so they can be published and reused across
internal builds. Applying the published plugins gives you consistent toolchains, testing strategy, and publishing
metadata across every module in a build.

## Highlights

- Single entry point (`io.github.jongminchung.spring-boot-app`) that layers Java, Spring Boot, dependency management,
  Lombok, Error Prone/NullAway, JVM test suites, Jacoco, and Maven Publish defaults (`plugins/spring-boot-app-plugin`).
- Reusable convention plugins (`conventions/*`) keep concerns focused—Java packaging, Spring Boot wiring, Jacoco
  aggregation, Maven Publish hardening, JVM test suites, and static analysis.
- Root build enforces Spotless formatting, JDK toolchains, shared reporting, and Maven coordinates so every subproject
  ships with the same quality bar.

## Repository Layout

- `plugins/spring-boot-app-plugin`: end-user Gradle plugin that applies every convention plugin in one go.
- `conventions/`: individual convention plugins (Java, Spring Boot, Jacoco, JVM test suite, Error Prone, publishing,
  etc.).
- `examples/`: runnable sample showing how to consume the Spring Boot application plugin.
- `docs/`: background notes and the detailed project evaluation (`docs/project-analysis.md`).

## Getting Started

1. Publish the plugins to your local Maven repository:
   ```bash
   ./gradlew publishToMavenLocal
   ```
2. In a consumer project, apply the Spring Boot application plugin:
   ```kotlin
   plugins {
       id("io.github.jongminchung.spring-boot-app") version "<released-version>"
   }
   ```
3. Regenerate dependencies if you are iterating locally:
   ```bash
   ./gradlew --refresh-dependencies compileJava
   ```
   See `examples/README.md` for a complete walkthrough that uses `examples/spring-boot-app-example`.

## Local Development

- Format and run the default suite (unit + functional tests):
  ```bash
  ./gradlew clean check
  ```
- Produce the aggregated Jacoco report configured in `build.gradle.kts`:
  ```bash
  ./gradlew testCodeCoverageReport
  ```
- Publish snapshots for manual testing:
  ```bash
  ./gradlew publishToMavenLocal
  ```

## Documentation

Additional analysis, improvement ideas, and design notes live in `docs/project-analysis.md`. Use it as a starting point
for prioritising future work or sharing the project goals with collaborators.
