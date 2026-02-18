package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class PublishingPluginTest : StringSpec(
  {

    "plugin creates maven publication and sourcesJar task" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.publishing")
      }

      group = "com.example"
      version = "0.1.0"
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("tasks", "--all")
        .build()

      result.output shouldContain "sourcesJar"
    }

    "publishToMavenLocal succeeds" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.publishing")
      }

      group = "com.example"
      version = "0.1.0"
      """.trimIndent(),
      )

      projectDir.resolve("src/main/java").mkdirs()

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("publishToMavenLocal")
        .build()

      result.output shouldContain "BUILD SUCCESSFUL"
    }
  },
)
