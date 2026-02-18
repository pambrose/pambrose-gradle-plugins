package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
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
      result.output shouldContain "SHOW_STANDARD_STREAMS=true"
      result.output shouldContain "EXCEPTION_FORMAT=FULL"
    }
  },
)
