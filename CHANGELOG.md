# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.5] - 2026-09-13

### Changed
- Upgraded Kotest to 6.2.5 and the ben-manes versions plugin to 0.62.0.
- The default `kotest-runner-junit5` version that `TestingPlugin` injects now tracks the bump above (6.2.5). Override with `pambroseTesting.kotestVersion` to pin the previous value. The injected `logback-classic` default is unchanged at 1.6.3.

## [1.1.4] - 2026-09-07

### Changed
- Upgraded the Kotlin Gradle Plugin to 2.4.20. This is the version the plugins themselves are compiled with; it does not change any version injected into consuming projects.
- The `TestingPluginTest` GradleRunner fixtures now apply `kotlin("jvm") version "2.4.20"` instead of the stale `2.4.0`, so the functional tests exercise the same Kotlin version the project builds with.

## [1.1.3] - 2026-09-02

### Fixed
- **`TestingPlugin`'s default `logback-classic` version no longer points at a nonexistent release.** 1.1.2 shipped `1.6.4`, which was never published to Maven Central, so every consuming project that applied `com.pambrose.testing` without overriding `pambroseTesting.logbackVersion` failed at dependency resolution. The default is now `1.6.3`, the current release.

### Added
- A regression test that resolves `testRuntimeClasspath` and `testCompileClasspath` against Maven Central rather than only asserting the dependency is declared. The pre-existing tests passed with the broken 1.6.4 default; this one fails on any injected version that does not resolve.

### Changed
- Trimmed the `Build Commands` and `Tech Stack` sections from `CLAUDE.md`. Both were reconstructible from `gradle/libs.versions.toml`, `gradle.properties`, and `build.gradle.kts`, and the Tech Stack block had already drifted out of date twice.

## [1.1.2] - 2026-09-02

### Added
- `.gitattributes` normalizing all text files to LF in the repository, forcing CRLF in the working tree for `*.bat` / `*.cmd` so `gradlew.bat` behaves on Windows, pinning `gradlew` and `*.sh` to LF, marking binary file types so Git never normalizes them, and flagging the Gradle wrapper files as `linguist-generated` to reduce diff noise on GitHub. Repository hygiene only; not part of any published artifact.

### Changed
- Upgraded the Gradle wrapper to 9.7.1, Kotest to 6.2.4, Kotlinter to 5.7.0, logback to 1.6.4, and the ben-manes versions plugin to 0.61.0.
- The default `logback-classic` and `kotest-runner-junit5` versions that `TestingPlugin` injects now track the bumps above (1.6.4 and 6.2.4). Override with `pambroseTesting.logbackVersion` / `pambroseTesting.kotestVersion` to pin the previous values.
- `KotlinterPlugin` now applies kotlinter 5.7.0.

## [1.1.1] - 2026-07-30

### Changed
- Upgraded Kotlin to 2.4.10, Kotest to 6.2.3, Kotlinter to 5.6.0, logback to 1.6.1, and the ben-manes versions plugin to 0.57.0.
- The default `logback-classic` and `kotest-runner-junit5` versions that `TestingPlugin` injects now track the bumps above (1.6.1 and 6.2.3). Override with `pambroseTesting.logbackVersion` / `pambroseTesting.kotestVersion` to pin the previous values.
- The ben-manes versions plugin is now applied under its current ID, `io.github.ben-manes.versions`, instead of the legacy `com.github.ben-manes.versions`. This affects the root build only; it is not part of any published plugin.

### Fixed
- Removed the Gradle 9.6 deprecation warnings emitted while configuring the root build. The `generateBuildConfig` task now uses `tasks.register(name)` instead of the deprecated `by tasks.registering` delegate, and the vanniktech `GradlePlugin` publication is configured with `sourcesJar = SourcesJar.Sources()` instead of the deprecated `Boolean` overload. No behavior change.

## [1.1.0] - 2026-07-02

### Removed
- `StableVersionsPlugin` and the `com.pambrose.stable-versions` plugin, along with its test and the now-unused `gradle-versions-plugin` dependency. **Breaking change:** consumers applying `com.pambrose.stable-versions` must remove it and apply `com.github.ben-manes.versions` directly if they still need dependency-update reporting. The root project retains its own `dependencyUpdates` configuration.

### Changed
- Upgraded the Gradle wrapper to 9.6.1, Kotest to 6.2.1, and the vanniktech `maven-publish` plugin to 0.37.0.

## [1.0.15] - 2026-06-05

### Added
- `TestingPlugin` now includes `TestLogEvent.STANDARD_ERROR` in its default test logging events (#19), so test stderr is surfaced in the build log even with `showStandardStreams = false`.
- The root project's own test logging mirrors the same `STANDARD_ERROR` event.

### Changed
- Upgraded Kotlin to 2.4.0 and the Gradle wrapper to 9.5.1.
- Bumped Kotest to 6.1.11 and Kotlinter to 5.5.0.
- Centralized versions in `gradle/libs.versions.toml` and consolidated build / Makefile configuration (#18).
- Renamed the `gradle` entry in `libs.versions.toml` to `gradle-wrapper` to make its purpose explicit; the Makefile's `GRADLE_VERSION` extraction was updated accordingly.

### Fixed
- Makefile `.PHONY` now declares the helper targets (`_check-gpg-env`, `_require-version`, `_require-gradle-version`) and drops the stale `depends` entry.
- Publish targets now depend on `_require-version` so a missing `version=` in `gradle.properties` fails fast instead of silently publishing a bogus artifact.

## [1.0.14] - 2026-04-22

### Added
- `TestingPlugin` adds `io.kotest:kotest-runner-junit5` to `testImplementation` by default when the `org.jetbrains.kotlin.jvm` plugin is applied. Configurable via `pambroseTesting.addKotest` and `pambroseTesting.kotestVersion`.
- `TestingPlugin` adds `org.jetbrains.kotlin:kotlin-test` (unversioned, resolves to the Kotlin plugin's version) to `testImplementation` by default when the `org.jetbrains.kotlin.jvm` plugin is applied. Configurable via `pambroseTesting.addKotlinTest`.
- Kotest version exposed on the generated `BuildConfig` object as `DEFAULT_KOTEST_VERSION` so `dependencyUpdates` can track it.

## [1.0.13] - 2026-04-22

### Added
- `TestingPlugin` adds `ch.qos.logback:logback-classic` to `testRuntimeOnly` by default.
- Generated internal `com.pambrose.BuildConfig` object exposes versions from `gradle/libs.versions.toml` (e.g. logback) to plugin code so `dependencyUpdates` can track them.
- Dokka documentation support and GitHub Actions workflow (#15).
- README badges and updated title (#14).

### Changed
- Signing configuration fixed for local publishing; `GPG_ENV` extracted in the Makefile (#12).

### Fixed
- Duplicate publication warning; JVM memory bumped for the build (#13).

## [1.0.12] - 2026-04-03

### Added
- Maven Central distribution via the vanniktech `maven-publish` plugin.
- Dokka-generated javadoc JAR and GPG signing for releases.

### Removed
- `ReposPlugin` and `SnapshotPlugin` (and their tests).
- JitPack distribution; Maven Central is now the primary channel.

### Changed
- All documentation updated to reflect Maven Central publishing.

## [1.0.11] - 2026-03-30

### Changed
- Upgraded Gradle wrapper to 9.4.1.
- Bumped Kotest to 6.1.10.

## [1.0.10] - 2026-03-15

### Changed
- Upgraded Gradle wrapper to 9.4.0.
- Bumped Kotest to 6.1.7.

## [1.0.9] - 2026-03-04

### Changed
- Version and documentation housekeeping.

## [1.0.8] - 2026-03-01

### Added
- `CODE_REVIEW.md` and `llms.txt`.
- Missing `ReposPlugin` and `KotlinterPlugin` entries in `CLAUDE.md` plugins table.
- Explicit `showStandardStreams = false` in `TestingPlugin` with a matching test.

### Changed
- `EnvVarPlugin` trims env var keys and values after splitting on `=`.
- Simplified `StableVersionsPlugin.isNonStable` double negation.
- `SnapshotPlugin` uses `configurations.configureEach` instead of `configurations.all`.
- Makefile derives `VERSION` from `build.gradle.kts` instead of hardcoding it.
- Bumped Kotest to 6.1.4.

### Fixed
- "testinging" typo in the README plugin IDs and examples.
- Removed unnecessary `afterEvaluate` in `PublishingPlugin`.

## [1.0.7] - 2026-02-24

### Changed
- Disabled standard output streams in test logging configuration.

## [1.0.6] - 2026-02-21

### Added
- Build trigger and view commands in the Makefile.

### Changed
- Cleaned up test logging configuration in `TestingPlugin`.
- Formatting fixes in `misc.xml`.

## [1.0.5] - 2026-02-21

### Added
- `vars` property on `EnvVarExtension` exposing accessible environment variables.
- Environment variables wired into `JavaExec` and `Test` tasks.
- Apache 2.0 license file.

### Changed
- Project renamed to `pambrose-gradle-plugins`.
- Test logging events updated in `TestingPlugin`.

## [1.0.4] - 2026-02-19

### Added
- `envvar` extension allowing customization of the environment variable file path.

### Changed
- Enhanced dependency updates configuration.

## [1.0.3] - 2026-02-18

### Fixed
- Marked the dependency updates plugin as incompatible with the configuration cache.

## [1.0.2] - 2026-02-18

### Added
- `TestingPlugin` for JUnit Platform configuration.

### Changed
- Renamed `ExcludeBetasPlugin` to `StableVersionsPlugin`.
- Refactored dependency management to use the Gradle version catalog.

## [1.0.1] - 2026-02-17

### Added
- `ReposPlugin` for repository management.
- `KotlinterPlugin` for kotlinter integration.
- Java Gradle Plugin setup.
- Maven Central usage instructions in README.

### Changed
- Group ID updated to `com.pambrose.gradle-plugins`.
- Refactored package name variable for clarity.

## [1.0.0] - 2026-02-17

### Added
- Initial Gradle plugin project structure with multiple plugins and configuration files.
- Project renamed from `common-gradle` to `gradle-plugins`.

[1.1.5]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.1.4...1.1.5
[1.1.4]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.1.3...1.1.4
[1.1.3]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.1.2...1.1.3
[1.1.2]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.1.1...1.1.2
[1.1.1]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.1.0...1.1.1
[1.1.0]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.15...1.1.0
[1.0.15]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.14...1.0.15
[1.0.14]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.13...1.0.14
[1.0.13]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.12...1.0.13
[1.0.12]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.11...1.0.12
[1.0.11]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.10...1.0.11
[1.0.10]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.9...1.0.10
[1.0.9]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.8...1.0.9
[1.0.8]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.7...1.0.8
[1.0.7]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.6...1.0.7
[1.0.6]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.5...1.0.6
[1.0.5]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.4...1.0.5
[1.0.4]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.3...1.0.4
[1.0.3]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.2...1.0.3
[1.0.2]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/pambrose/pambrose-gradle-plugins/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/pambrose/pambrose-gradle-plugins/releases/tag/1.0.0
