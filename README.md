# gradle-plugins

Shared Gradle convention plugins for Kotlin JVM projects.

## Available Plugins

### `com.pambrose.test`

Configures all test tasks to use JUnit Platform with verbose logging (passed/skipped/failed events and standard
streams).

### `com.pambrose.publishing`

Applies `maven-publish` and `java`, generates a sources JAR, and creates a `maven` publication from the `java`
component.

### `com.pambrose.exclude-betas`

Configures the ben-manes `dependencyUpdates` task to reject RC, beta, alpha, and milestone versions.

### `com.pambrose.envvar`

Loads environment variables from `secrets/secrets.env` into all `JavaExec` and `Test` tasks. Lines starting with `#`
and blank lines are ignored.

### `com.pambrose.snapshot`

Disables Gradle caching for changing (SNAPSHOT) modules so they are always re-resolved.

## Usage

### From JitPack

In your **settings.gradle.kts**, add JitPack with a `resolutionStrategy` to map plugin IDs to the JitPack artifact:

```kotlin
pluginManagement {
  resolutionStrategy {
    eachPlugin {
      if (requested.id.namespace == "com.pambrose") {
        useModule("com.github.pambrose:gradle-plugins:${requested.version}")
      }
    }
  }
  repositories {
    maven("https://jitpack.io")
    gradlePluginPortal()
  }
}
```

The `resolutionStrategy` is required because JitPack publishes artifacts under `com.github.pambrose:gradle-plugins`,
while Gradle plugin resolution expects a plugin marker artifact. The strategy redirects `com.pambrose.*` plugin
requests to the correct JitPack coordinates.

In your **build.gradle.kts**, apply the desired plugins using a Git tag, commit hash, or `main-SNAPSHOT`:

```kotlin
plugins {
  id("com.pambrose.test") version "Tag"
  id("com.pambrose.publishing") version "Tag"
  id("com.pambrose.exclude-betas") version "Tag"
  id("com.pambrose.envvar") version "Tag"
  id("com.pambrose.snapshot") version "Tag"
}
```

Replace `Tag` with a GitHub release tag (e.g., `1.0.1`), a short commit hash, or `main-SNAPSHOT` for the latest commit
on `main`.

### From Maven Central

In your **settings.gradle.kts**, add Maven Central to the plugin repositories:

```kotlin
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}
```

In your **build.gradle.kts**, apply the desired plugins:

```kotlin
plugins {
  id("com.pambrose.test") version "1.0.1"
  id("com.pambrose.publishing") version "1.0.1"
  id("com.pambrose.exclude-betas") version "1.0.1"
  id("com.pambrose.envvar") version "1.0.1"
  id("com.pambrose.snapshot") version "1.0.1"
}
```

No `resolutionStrategy` is needed — Gradle resolves plugin marker artifacts directly from Maven Central.

### From Local Maven

Publish the plugins locally:

```bash
./gradlew publishToMavenLocal
```

In your **settings.gradle.kts**:

```kotlin
pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
  }
}
```

In your **build.gradle.kts**:

```kotlin
plugins {
  id("com.pambrose.test") version "1.0.1"
  id("com.pambrose.publishing") version "1.0.1"
  id("com.pambrose.exclude-betas") version "1.0.1"
  id("com.pambrose.envvar") version "1.0.1"
  id("com.pambrose.snapshot") version "1.0.1"
}
```

## Building

```bash
./gradlew build
```