package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class TestingPluginTest : StringSpec(
  {

    "plugin configures JUnit Platform with verbose logging" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      tasks.register("showTestConfig") {
        doLast {
          tasks.withType<Test>().forEach { testTask ->
            val junitEnabled = testTask.options is org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
            println("JUNIT_PLATFORM=${'$'}junitEnabled")
            println("SHOW_STANDARD_STREAMS=${'$'}{testTask.testLogging.showStandardStreams}")
            println("EXCEPTION_FORMAT=${'$'}{testTask.testLogging.exceptionFormat}")
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showTestConfig")
        .build()

      result.output shouldContain "JUNIT_PLATFORM=true"
      result.output shouldContain "SHOW_STANDARD_STREAMS=false"
      result.output shouldContain "EXCEPTION_FORMAT=FULL"
    }

    "plugin adds logback-classic to testRuntimeOnly by default" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      tasks.register("showTestDeps") {
        doLast {
          configurations["testRuntimeOnly"].dependencies.forEach { d ->
            println("DEP=${'$'}{d.group}:${'$'}{d.name}:${'$'}{d.version}")
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showTestDeps")
        .build()

      result.output shouldContain "DEP=ch.qos.logback:logback-classic:"
    }

    // Regression guard: the tests above assert only that the dependency is DECLARED, so a
    // default version that does not exist on Maven Central passes them. 1.1.2 shipped
    // logback 1.6.4, which was never published, and every consumer using the default broke.
    // This test actually resolves the configurations, so a bad version fails the build.
    "injected default dependency versions resolve against Maven Central" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        kotlin("jvm") version "2.4.21"
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      tasks.register("resolveTestDeps") {
        doLast {
          configurations["testRuntimeClasspath"].resolve().forEach { println("RUNTIME=${'$'}{it.name}") }
          configurations["testCompileClasspath"].resolve().forEach { println("COMPILE=${'$'}{it.name}") }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("resolveTestDeps")
        .build()

      result.output shouldContain "RUNTIME=logback-classic-${BuildConfig.DEFAULT_LOGBACK_VERSION}.jar"
      result.output shouldContain "kotest-runner-junit5"
    }

    "plugin honors addLogbackClassic = false" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      pambroseTesting {
        addLogbackClassic.set(false)
      }

      tasks.register("showTestDeps") {
        doLast {
          configurations["testRuntimeOnly"].dependencies.forEach { d ->
            println("DEP=${'$'}{d.group}:${'$'}{d.name}:${'$'}{d.version}")
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showTestDeps")
        .build()

      result.output shouldNotContain "logback-classic"
    }

    "plugin adds kotest and kotlin-test to testImplementation by default" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        kotlin("jvm") version "2.4.21"
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      tasks.register("showTestImpl") {
        doLast {
          configurations["testImplementation"].dependencies.forEach { d ->
            println("DEP=${'$'}{d.group}:${'$'}{d.name}:${'$'}{d.version}")
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showTestImpl")
        .build()

      result.output shouldContain "DEP=io.kotest:kotest-runner-junit5:"
      result.output shouldContain "DEP=org.jetbrains.kotlin:kotlin-test:"
    }

    "plugin honors addKotest = false and addKotlinTest = false" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        kotlin("jvm") version "2.4.21"
        id("com.pambrose.testing")
      }

      repositories {
        mavenCentral()
      }

      pambroseTesting {
        addKotest.set(false)
        addKotlinTest.set(false)
      }

      tasks.register("showTestImpl") {
        doLast {
          configurations["testImplementation"].dependencies.forEach { d ->
            println("DEP=${'$'}{d.group}:${'$'}{d.name}:${'$'}{d.version}")
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showTestImpl")
        .build()

      result.output shouldNotContain "kotest-runner-junit5"
      result.output shouldNotContain "kotlin-test"
    }
  },
)
