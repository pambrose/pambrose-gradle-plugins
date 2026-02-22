import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  `kotlin-dsl`
  `maven-publish`
  `java-gradle-plugin`
  alias(libs.plugins.ben.manes.versions)
}

group = "com.pambrose.gradle-plugins"
version = "1.0.7"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.gradle.versions.plugin)
  implementation(libs.kotlinter.gradle)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
}

kotlin {
  jvmToolchain(17)
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    listOf("-RC", "-BETA", "-ALPHA", "-M").any { candidate.version.uppercase().contains(it) }
  }
}

fun NamedDomainObjectContainer<PluginDeclaration>.plugin(
  name: String,
  id: String,
) {
  val packageName = "com.pambrose"
  create(name) {
    this.id = "$packageName.$id"
    this.implementationClass = "$packageName.$name"
  }
}

gradlePlugin {
  plugins {
    plugin("EnvVarPlugin", "envvar")
    plugin("StableVersionsPlugin", "stable-versions")
    plugin("PublishingPlugin", "publishing")
    plugin("ReposPlugin", "repos")
    plugin("SnapshotPlugin", "snapshot")
    plugin("KotlinterPlugin", "kotlinter")
    plugin("TestingPlugin", "testing")
  }
}