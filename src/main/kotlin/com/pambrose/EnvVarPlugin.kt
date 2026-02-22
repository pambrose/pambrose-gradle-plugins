package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType

open class EnvVarExtension {
  var filename: String = "secrets/secrets.env"
  val vars: MutableMap<String, String> = mutableMapOf()
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

        // Make the envVars available to other plugins
        extension.vars.putAll(envVars)

        /*
        Finds all tasks of type JavaExec and Test (both existing and any added later) and
        adds envVars to each task's process environment. When those tasks run, the child
        JVM process will have those environment variables set — alongside the system's
        existing environment variables.
         */
        tasks.withType<JavaExec> { environment(envVars) }
        tasks.withType<Test> { environment(envVars) }
      }
    }
  }
}
