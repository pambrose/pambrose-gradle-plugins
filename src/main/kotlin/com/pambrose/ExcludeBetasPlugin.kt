package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class ExcludeBetasPlugin : Plugin<Project> {
  override fun apply(project: Project) {
//    project.pluginManager.apply("org.jetbrains.kotlin.jvm")
//
//    project.repositories.mavenCentral()
//
//    project.extensions.configure(KotlinJvmExtension::class.java) {
//      jvmToolchain(21)
//    }

    fun isNonStable(version: String): Boolean {
      // val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
      // val regex = "^[0-9,.v-]+(-r)?$".toRegex()
      val betaKeyword = listOf("-RC", "-BETA", "-ALPHA", "-M").any { version.uppercase().contains(it) }
      val isStable = !betaKeyword // (stableKeyword || regex.matches(version)) && !betaKeyword
      return !isStable
    }

    project.tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
      rejectVersionIf {
        isNonStable(candidate.version)
      }
    }
  }
}
