package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jmailen.gradle.kotlinter.KotlinterExtension

class KotlinterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      pluginManager.apply("org.jmailen.kotlinter")

      extensions.configure(KotlinterExtension::class.java) {
        reporters = arrayOf("checkstyle", "plain")
      }
    }
  }
}