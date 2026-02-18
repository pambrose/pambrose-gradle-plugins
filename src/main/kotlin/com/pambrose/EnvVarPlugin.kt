package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

class EnvVarPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val secretsFile = file("secrets/secrets.env")
      if (secretsFile.exists()) {
        val envVars =
          secretsFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
              val idx = line.indexOf('=')
              if (idx > 0) line.substring(0, idx) to line.substring(idx + 1) else null
            }
            .toMap()

        tasks.withType<JavaExec> { environment(envVars) }
        tasks.withType<Test> { environment(envVars) }
      }
    }
  }
}
