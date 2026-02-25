package com.pambrose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType

class TestingPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      // pluginManager.apply("org.jetbrains.kotlin.jvm")

      tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
          events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
          exceptionFormat = TestExceptionFormat.FULL
          // showStandardStreams = true
        }
      }
    }
  }
}
