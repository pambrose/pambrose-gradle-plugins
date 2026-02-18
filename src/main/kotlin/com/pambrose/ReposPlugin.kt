package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project

class ReposPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      repositories.google()
      repositories.mavenCentral()
      repositories.maven { url = uri("https://jitpack.io") }
    }
  }
}
