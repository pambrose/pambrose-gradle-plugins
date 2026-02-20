package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType

open class EnvVarExtension {
  var filename: String = "secrets/secrets.env"
}

class EnvVarPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create<EnvVarExtension>("envvar")

    project.afterEvaluate {
      val secretsFile = file(extension.filename)
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
