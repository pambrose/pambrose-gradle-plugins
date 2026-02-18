# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A multi-plugin Gradle convention plugin project built with Kotlin DSL. These plugins are consumed by other
Gradle projects to share common build configuration. Distributed via JitPack (`com.github.pambrose:gradle-plugins`).

## Build Commands

- **Build:** `./gradlew build`
- **Clean:** `./gradlew clean`
- **Publish to local Maven repo:** `./gradlew publishToMavenLocal`
- **Check dependency updates:** `./gradlew dependencyUpdates`

## Tech Stack

- Gradle 9.2.0 with Kotlin DSL
- Kotlin Gradle Plugin 2.3.10
- Ben-Manes Versions Plugin 0.53.0
- Group/artifact: `com.pambrose.gradle:gradle-plugins:1.0.2`

## Architecture

All plugins live in `src/main/kotlin/com/pambrose/`, each implementing `Plugin<Project>`. Plugins are
registered in the `gradlePlugin` block of `build.gradle.kts` using a `plugin()` helper function that
auto-derives the plugin ID (`com.pambrose.<id>`) and implementation class (`com.pambrose.<Name>`) from
the arguments.

### Plugins

| Plugin ID                      | Class                  | Purpose                                                                    |
|--------------------------------|------------------------|----------------------------------------------------------------------------|
| `com.pambrose.test`            | `TestPlugin`           | Configures JUnit Platform with verbose test logging                        |
| `com.pambrose.publishing`      | `PublishingPlugin`     | Sets up `maven-publish` with sources JAR and a Maven publication           |
| `com.pambrose.stable-versions` | `StableVersionsPlugin` | Filters RC/beta/alpha/milestone versions from `dependencyUpdates` results  |
| `com.pambrose.envvar`          | `EnvVarPlugin`         | Loads env vars from `secrets/secrets.env` into `JavaExec` and `Test` tasks |
| `com.pambrose.snapshot`        | `SnapshotPlugin`       | Disables Gradle caching for changing (SNAPSHOT) modules                    |

### Adding a New Plugin

1. Create a new `Plugin<Project>` class in `src/main/kotlin/com/pambrose/`
2. Register it in the `gradlePlugin.plugins` block in `build.gradle.kts` using `plugin("ClassName", "id-suffix")`