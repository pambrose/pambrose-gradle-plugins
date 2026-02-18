package com.pambrose

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createTempDirectory
import org.gradle.testkit.runner.GradleRunner

class ReposPluginTest : StringSpec(
  {

    "plugin adds Google, MavenCentral, and JitPack repositories" {
      val projectDir = createTempDirectory("test").toFile()
      projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")
      projectDir.resolve("build.gradle.kts").writeText(
        """
      plugins {
        java
        id("com.pambrose.repos")
      }

      tasks.register("showRepos") {
        doLast {
          repositories.forEach { repo ->
            when (repo) {
              is org.gradle.api.artifacts.repositories.MavenArtifactRepository ->
                println("REPO=${'$'}{repo.url}")
            }
          }
        }
      }
      """.trimIndent(),
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("showRepos")
        .build()

      result.output shouldContain "google"
      result.output shouldContain "jitpack.io"
    }
  },
)
