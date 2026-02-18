package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

class PublishingPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      pluginManager.apply("maven-publish")
      pluginManager.apply("java")

      extensions.configure<JavaPluginExtension> {
        withSourcesJar()
      }

      afterEvaluate {
        extensions.configure<PublishingExtension> {
          publications {
            create<MavenPublication>("maven") {
              from(components["java"])
            }
          }
        }
      }
    }
  }
}