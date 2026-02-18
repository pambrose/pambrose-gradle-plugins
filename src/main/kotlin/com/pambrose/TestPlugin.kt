package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType

class TestPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      // pluginManager.apply("org.jetbrains.kotlin.jvm")
      // repositories.mavenCentral()

      tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
          // events("passed", "skipped", "failed", "standardOut", "standardError")
          events = TestLogEvent.entries.toSet() // setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
          exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
          showStandardStreams = true
        }
      }
    }
  }
}
