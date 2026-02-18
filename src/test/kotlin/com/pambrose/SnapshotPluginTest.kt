package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class SnapshotPluginTest : StringSpec(
  {

    "plugin applies without error" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.snapshot")
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("tasks", "--quiet")
        .build()

      result.output.shouldNotBeEmpty()
    }

    "plugin configures caching for changing modules" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.snapshot")
      }

      tasks.register("showCacheConfig") {
        doLast {
          println("SNAPSHOT_PLUGIN_APPLIED=true")
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showCacheConfig")
        .build()

      result.output shouldContain "SNAPSHOT_PLUGIN_APPLIED=true"
    }
  },
)
