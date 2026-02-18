package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class KotlinterPluginTest : StringSpec(
  {

    "plugin registers lintKotlin and formatKotlin tasks" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        kotlin("jvm") version "2.3.10"
        id("com.pambrose.kotlinter")
      }

      repositories {
        mavenCentral()
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("tasks", "--all")
        .build()

      result.output shouldContain "lintKotlin"
      result.output shouldContain "formatKotlin"
    }

    "plugin configures reporters as checkstyle and plain" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        kotlin("jvm") version "2.3.10"
        id("com.pambrose.kotlinter")
      }

      repositories {
        mavenCentral()
      }

      tasks.register("showKotlinterConfig") {
        doLast {
          val ext = project.extensions.getByType(org.jmailen.gradle.kotlinter.KotlinterExtension::class.java)
          println("REPORTERS=${'$'}{ext.reporters.joinToString(",")}")
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showKotlinterConfig")
        .build()

      result.output shouldContain "REPORTERS=checkstyle,plain"
    }
  },
)
