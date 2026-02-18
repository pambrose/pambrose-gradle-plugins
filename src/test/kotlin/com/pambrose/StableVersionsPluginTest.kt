package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class StableVersionsPluginTest : StringSpec(
  {

    listOf("1.0.0", "2.3.10", "5.11.4", "0.53.0", "1.0.0-r").forEach { version ->
      "stable version $version is detected as stable" {
        StableVersionsPlugin.isNonStable(version).shouldBeFalse()
      }
    }

    listOf("1.0.0-RC1", "2.0.0-beta1", "3.0.0-alpha01", "1.0.0-M1", "2.0.0-rc.2", "1.0.0-ALPHA3").forEach { version ->
      "non-stable version $version is detected as non-stable" {
        StableVersionsPlugin.isNonStable(version).shouldBeTrue()
      }
    }

    "plugin applies and dependencyUpdates task works" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        id("com.github.ben-manes.versions")
        id("com.pambrose.stable-versions")
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("dependencyUpdates", "--dry-run")
        .build()

      result.output shouldContain ":dependencyUpdates SKIPPED"
    }
  },
)
