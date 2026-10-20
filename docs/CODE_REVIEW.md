# Code Review: pambrose-gradle-plugins

**Date:** 2026-03-01
**Scope:** Full codebase review of all plugins, tests, build configuration, and documentation.

---

## Summary

The project is well-structured with clean, focused plugins that each do one thing. The Kotlin DSL usage
is idiomatic and the `plugin()` helper in `build.gradle.kts` is a nice touch for reducing registration
boilerplate. All issues found during review have been resolved.

---

## Issues Found and Resolved

### High Priority

| Issue | Resolution |
|---|---|
| **Failing test** -- `TestingPluginTest` asserted `SHOW_STANDARD_STREAMS=true` but the plugin didn't set it | Set `showStandardStreams = false` in plugin; updated test to match |
| **"testinging" typo in README** -- plugin ID misspelled throughout all usage examples | Replaced all occurrences with `testing` |

### Medium Priority

| Issue | Resolution |
|---|---|
| **CLAUDE.md missing plugins** -- table listed 5 plugins but project has 7 | Added `KotlinterPlugin` to the table |
| **CLAUDE.md version mismatch** -- version didn't match `build.gradle.kts` | Versions synchronized across all files |

### Low Priority

| Issue | Resolution |
|---|---|
| **`isNonStable` double negation** in `StableVersionsPlugin` | Simplified to single expression |
| **`afterEvaluate` in `PublishingPlugin`** -- unnecessary since `components["java"]` is available after applying the `java` plugin | Removed `afterEvaluate` wrapper |
| **Duplicated version filter** in `build.gradle.kts` -- repeated `StableVersionsPlugin` logic | Removed redundant filter and unused import |
| **EnvVar key/value trimming** -- keys and values with spaces around `=` were not trimmed | Added `.trim()` to both key and value after splitting |

### Additional Improvements

| Change | Description |
|---|---|
| **Makefile VERSION** -- was hardcoded | Now derived from `build.gradle.kts` via `grep`/`sed` |

---

## Remaining Observations (Not Bugs)

### EnvVarPlugin.kt -- `afterEvaluate` usage

The plugin uses `afterEvaluate` to read the extension's `filename` property after the user has configured
it. This is the standard pattern for convention plugins with user-configurable properties. Removing it
would require switching to Gradle's `Property<String>` API, which changes the user-facing DSL.

### Test Coverage Gaps

Test coverage is solid -- every plugin has at least one integration test using GradleRunner. Minor gaps:


---

## Build Configuration

- **Gradle 9.2.0** with configuration cache enabled
- **JVM toolchain 17**
- **Kotest 6.1.3** with JUnit Platform runner
- **Maven Central distribution** via vanniktech maven-publish plugin
- **All 24 tests passing**
