package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class StableVersionsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
      rejectVersionIf {
        isNonStable(candidate.version)
      }
    }
  }

  companion object {
    internal fun isNonStable(version: String): Boolean {
      val betaKeyword = listOf("-RC", "-BETA", "-ALPHA", "-M").any { version.uppercase().contains(it) }
      val isStable = !betaKeyword
      return !isStable
    }
  }
}
