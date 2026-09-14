# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A multi-plugin Gradle convention plugin project built with Kotlin DSL. These plugins are consumed by other
Gradle projects to share common build configuration. Distributed via Maven Central (`com.pambrose:pambrose-gradle-plugins`).

## Architecture

All plugins live in `src/main/kotlin/com/pambrose/`, each implementing `Plugin<Project>`. Plugins are
registered in the `gradlePlugin` block of `build.gradle.kts` using a `plugin()` helper function that
auto-derives the plugin ID (`com.pambrose.<id>`) and implementation class (`com.pambrose.<Name>`) from
the arguments.

### Plugins

| Plugin ID                      | Class                  | Purpose                                                                    |
|--------------------------------|------------------------|----------------------------------------------------------------------------|
| `com.pambrose.testing`         | `TestingPlugin`        | Configures JUnit Platform with verbose test logging; adds logback-classic to `testRuntimeOnly`, and kotest-runner-junit5 + kotlin-test to `testImplementation` (Kotlin JVM projects) by default |
| `com.pambrose.publishing`      | `PublishingPlugin`     | Sets up `maven-publish` with sources JAR and a Maven publication           |
| `com.pambrose.envvar`          | `EnvVarPlugin`         | Loads env vars from `.env` into `JavaExec` and `Test` tasks |
| `com.pambrose.kotlinter`       | `KotlinterPlugin`      | Applies kotlinter with checkstyle and plain reporters                      |

### Adding a New Plugin

1. Create a new `Plugin<Project>` class in `src/main/kotlin/com/pambrose/`
2. Register it in the `gradlePlugin.plugins` block in `build.gradle.kts` using `plugin("ClassName", "id-suffix")`

### Versions Referenced From Plugin Code

Runtime-injected dependency versions (e.g. logback-classic and kotest in `TestingPlugin`) are declared in
`gradle/libs.versions.toml` so `dependencyUpdates` and Dependabot can track them. The `generateBuildConfig` task
writes an internal `com.pambrose.BuildConfig` object into `build/generated/sources/buildconfig/`
that exposes these as Kotlin constants. To add another such version:

1. Add the entry to `[versions]` in `libs.versions.toml`, plus a `[libraries]` entry that references it
   via `version.ref`. Dependabot ignores `[versions]` entries no library or plugin references, even if
   nothing in the build uses the library.
2. Add a `const val` to the `generateBuildConfig` task's generated file in `build.gradle.kts`.
3. Reference it from the plugin as `BuildConfig.<NAME>`.

## Dependency Updates

`.github/dependabot.yml` opens weekly PRs for the version catalog and GitHub Actions, grouping minor and
patch bumps. Dependabot only edits the catalog, so a PR that bumps an injected default (logback, kotest)
or Kotlin still needs by hand: the `llms.txt` Tech Stack, the hardcoded `kotlin("jvm") version` in the
test fixtures, and the CHANGELOG/RELEASE_NOTES entries. The Gradle wrapper is excluded from Dependabot;
bump `gradle-wrapper` in `libs.versions.toml` and run `make upgrade-wrapper`.

## Releasing

The version lives in `gradle.properties` (`version=`). Everything else that names it must be
bumped by hand in the same commit — nothing derives it:

1. `gradle.properties` -- `version=<new>`
2. `README.md` -- plugin `version "<new>"` snippets, the `pambrose-plugins` catalog entry, and the
   Kotlin badge if the Kotlin version changed
3. `llms.txt` -- the `Group/artifact:` line, the usage snippet, and the `Tech Stack` versions
4. `CHANGELOG.md` -- a new `## [<new>] - YYYY-MM-DD` section plus a `[<new>]:` compare link at the
   bottom
5. `RELEASE_NOTES.md` -- a new `## v<new> — YYYY-MM-DD` narrative section at the top

`CLAUDE.md` intentionally carries no version numbers; the `Tech Stack` section that once did was
removed in 1.1.3 after drifting out of date twice.
