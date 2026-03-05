package com.pambrose

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class StableVersionsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      pluginManager.apply("com.github.ben-manes.versions")

      tasks.withType<DependencyUpdatesTask> {
        notCompatibleWithConfigurationCache("the dependency updates plugin is not compatible with the configuration cache")
        rejectVersionIf {
          isNonStable(candidate.version)
        }
      }
    }
  }

  companion object {
    private val UNSTABLE_QUALIFIERS = listOf("-RC", "-BETA", "-ALPHA", "-M")

    internal fun isNonStable(version: String): Boolean =
      UNSTABLE_QUALIFIERS.any { version.uppercase().contains(it) }
  }
}
