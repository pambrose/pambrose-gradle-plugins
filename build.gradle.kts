plugins {
  `kotlin-dsl`
  `maven-publish`
  id("java-gradle-plugin")
  id("com.github.ben-manes.versions") version "0.53.0"
}

group = "com.pambrose.gradle-plugins"
version = "1.0.2"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
  implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
  implementation("org.jmailen.gradle:kotlinter-gradle:5.4.2")
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
    plugin("TestPlugin", "test")
  }
}