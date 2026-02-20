package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class EnvVarPluginTest : StringSpec(
  {

    "plugin applies without secrets file" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.envvar")
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

    "plugin parses env file with comments, blanks, and values containing equals" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")

      projectDir.resolve("secrets").mkdirs()
      projectDir.resolve("secrets/secrets.env").writeText(
        """
      # This is a comment
      KEY1=value1

      KEY2=value2=extra
      KEY3=value3
      """.trimIndent(),
      )

      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.envvar")
      }

      tasks.register<JavaExec>("printEnv") {
        mainClass.set("does.not.Exist")
        doFirst {
          val env = environment
          require(env["KEY1"] == "value1") { "KEY1 expected value1 but got ${'$'}{env["KEY1"]}" }
          require(env["KEY2"] == "value2=extra") { "KEY2 expected value2=extra but got ${'$'}{env["KEY2"]}" }
          require(env["KEY3"] == "value3") { "KEY3 expected value3 but got ${'$'}{env["KEY3"]}" }
          println("ENV_VARS_OK")
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("printEnv", "--dry-run")
        .build()

      result.output shouldContain ":printEnv SKIPPED"
    }

    "plugin loads env vars from custom filename" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")

      projectDir.resolve("myfile.env").writeText("CUSTOM_KEY=custom_value")

      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.envvar")
      }

      envvar {
        filename = "myfile.env"
      }

      tasks.register<JavaExec>("printEnv") {
        mainClass.set("does.not.Exist")
        doFirst {
          val env = environment
          require(env["CUSTOM_KEY"] == "custom_value") { "CUSTOM_KEY expected custom_value but got ${'$'}{env["CUSTOM_KEY"]}" }
          println("CUSTOM_ENV_OK")
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("printEnv", "--dry-run")
        .build()

      result.output shouldContain ":printEnv SKIPPED"
    }

    "plugin loads env vars into Test tasks" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")

      projectDir.resolve("secrets").mkdirs()
      projectDir.resolve("secrets/secrets.env").writeText("MY_SECRET=hello123")

      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.envvar")
      }

      tasks.named<Test>("test") {
        doFirst {
          val env = environment
          require(env["MY_SECRET"] == "hello123") { "MY_SECRET not set" }
          println("TEST_ENV_OK")
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("test", "--dry-run")
        .build()

      result.output shouldContain ":test SKIPPED"
    }
  },
)
