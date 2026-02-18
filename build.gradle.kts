plugins {
  `kotlin-dsl`
  `maven-publish`
  id("com.github.ben-manes.versions") version "0.53.0"
}

group = "com.pambrose.gradle"
version = "1.0.1"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
  implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
}

fun NamedDomainObjectContainer<PluginDeclaration>.plugin(
  name: String,
  id: String,
) {
  val base = "com.pambrose"
  create(name) {
    this.id = "$base.$id"
    this.implementationClass = "$base.$name"
  }
}

gradlePlugin {
  plugins {
    plugin("EnvVarPlugin", "envvar")
    plugin("ExcludeBetasPlugin", "exclude-betas")
    plugin("PublishingPlugin", "publishing")
    plugin("SnapshotPlugin", "snapshot")
    plugin("TestPlugin", "test")
  }
}