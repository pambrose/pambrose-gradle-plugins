package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project

class SnapshotPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
      }
    }
  }
}
