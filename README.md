# gradle-plugins

Shared Gradle convention plugins for Kotlin JVM projects.

## Available Plugins

### `com.pambrose.testinging`

Configures all test tasks to use JUnit Platform with verbose logging (passed/skipped/failed events and standard
streams).

### `com.pambrose.publishing`

Applies `maven-publish` and `java`, generates a sources JAR, and creates a `maven` publication from the `java`
component.

### `com.pambrose.stable-versions`

Applies the ben-manes `versions` plugin and configures the `dependencyUpdates` task to reject RC, beta, alpha, and
milestone versions. Consumers do not need to apply `com.github.ben-manes.versions` separately.

### `com.pambrose.envvar`

Loads environment variables from `secrets/secrets.env` into all `JavaExec` and `Test` tasks. Lines starting with `#`
and blank lines are ignored.

### `com.pambrose.kotlinter`

Applies the [kotlinter](https://github.com/jeremymailen/kotlinter-gradle) plugin and configures it with `checkstyle` and
`plain` reporters.

### `com.pambrose.repos`

Adds Google, Maven Central, and JitPack repositories to the project.

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
  id("com.pambrose.envvar") version "Tag"
  id("com.pambrose.stable-versions") version "Tag"
  id("com.pambrose.kotlinter") version "Tag"
  id("com.pambrose.publishing") version "Tag"
  id("com.pambrose.repos") version "Tag"
  id("com.pambrose.snapshot") version "Tag"
  id("com.pambrose.testinging") version "Tag"
}
```

Replace `Tag` with a GitHub release tag (e.g., `1.0.3`), a short commit hash, or `main-SNAPSHOT` for the latest commit
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
  id("com.pambrose.envvar") version "1.0.3"
  id("com.pambrose.stable-versions") version "1.0.3"
  id("com.pambrose.kotlinter") version "1.0.3"
  id("com.pambrose.publishing") version "1.0.3"
  id("com.pambrose.repos") version "1.0.3"
  id("com.pambrose.snapshot") version "1.0.3"
  id("com.pambrose.testinging") version "1.0.3"
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
  id("com.pambrose.envvar") version "1.0.3"
  id("com.pambrose.stable-versions") version "1.0.3"
  id("com.pambrose.kotlinter") version "1.0.3"
  id("com.pambrose.publishing") version "1.0.3"
  id("com.pambrose.repos") version "1.0.3"
  id("com.pambrose.snapshot") version "1.0.3"
  id("com.pambrose.testinging") version "1.0.3"
}
```

### With Version Catalog (`libs.versions.toml`)

You can manage plugin versions centrally using a
[Gradle version catalog](https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog).

In your **gradle/libs.versions.toml**, define the version and plugin aliases:

```toml
[versions]
pambrose-plugins = "1.0.3"

[plugins]
pambrose-envvar = { id = "com.pambrose.envvar", version.ref = "pambrose-plugins" }
pambrose-stable-versions = { id = "com.pambrose.stable-versions", version.ref = "pambrose-plugins" }
pambrose-kotlinter = { id = "com.pambrose.kotlinter", version.ref = "pambrose-plugins" }
pambrose-publishing = { id = "com.pambrose.publishing", version.ref = "pambrose-plugins" }
pambrose-repos = { id = "com.pambrose.repos", version.ref = "pambrose-plugins" }
pambrose-snapshot = { id = "com.pambrose.snapshot", version.ref = "pambrose-plugins" }
pambrose-testing = { id = "com.pambrose.testinging", version.ref = "pambrose-plugins" }
```

In your **build.gradle.kts**, apply the plugins using `alias()`:

```kotlin
plugins {
  alias(libs.plugins.pambrose.envvar)
  alias(libs.plugins.pambrose.stable.versions)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.publishing)
  alias(libs.plugins.pambrose.repos)
  alias(libs.plugins.pambrose.snapshot)
  alias(libs.plugins.pambrose.testing)
}
```

The `pluginManagement` block in **settings.gradle.kts** is still required to configure the repository
(JitPack, Maven Central, or Maven Local) as shown in the sections above.

### Multi-Module Projects

In a multi-module project, declare plugin versions once in the root **build.gradle.kts** using `apply false`,
then apply them without a version in each subproject.

#### Root `build.gradle.kts`

Use `apply false` to resolve the plugin version without applying the plugin to the root project:

```kotlin
plugins {
  id("com.pambrose.envvar") version "1.0.3" apply false
  id("com.pambrose.stable-versions") version "1.0.3" apply false
  id("com.pambrose.kotlinter") version "1.0.3" apply false
  id("com.pambrose.publishing") version "1.0.3" apply false
  id("com.pambrose.repos") version "1.0.3" apply false
  id("com.pambrose.snapshot") version "1.0.3" apply false
  id("com.pambrose.testinging") version "1.0.3" apply false
}
```

#### Subproject `build.gradle.kts`

Apply only the plugins each subproject needs, without specifying a version:

```kotlin
plugins {
  id("com.pambrose.repos")
  id("com.pambrose.testinging")
  id("com.pambrose.kotlinter")
}
```

#### With Version Catalog

When using `libs.versions.toml`, the same pattern works with `alias()`:

Root **build.gradle.kts**:

```kotlin
plugins {
  alias(libs.plugins.pambrose.repos) apply false
  alias(libs.plugins.pambrose.testing) apply false
  alias(libs.plugins.pambrose.kotlinter) apply false
  // ... other plugins as needed
}
```

Subproject **build.gradle.kts**:

```kotlin
plugins {
  alias(libs.plugins.pambrose.repos)
  alias(libs.plugins.pambrose.testing)
  alias(libs.plugins.pambrose.kotlinter)
}
```

#### Applying to All Subprojects

To apply a plugin to every subproject, use a `subprojects` block in the root **build.gradle.kts**:

```kotlin
subprojects {
  apply(plugin = "com.pambrose.repos")
  apply(plugin = "com.pambrose.testinging")
}
```

## Building

```bash
./gradlew build
```