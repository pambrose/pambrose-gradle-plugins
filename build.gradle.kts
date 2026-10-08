import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.plugins.signing.Sign
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.ben.manes.versions)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
}

val groupId = group.toString()
val artifactId = "pambrose-gradle-plugins"
val githubUrl = "https://github.com/pambrose/$artifactId"

findProperty("overrideVersion")?.toString()?.let { version = it }

repositories {
  mavenCentral()
  gradlePluginPortal()
}

// Declares the dependency versions that plugins inject into consuming projects (see generateBuildConfig).
// Nothing in the build resolves them; the resolvable configuration exists only because dependencyUpdates
// skips configurations that cannot be resolved.
val injectedDefaults = configurations.dependencyScope("injectedDefaults")
configurations.resolvable("injectedDefaultsClasspath") {
  extendsFrom(injectedDefaults.get())
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlinter.gradle)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)

  injectedDefaults(libs.logback.classic)
  injectedDefaults(libs.kotest.runner.junit5)
}

kotlin {
  jvmToolchain(libs.versions.jvm.get().toInt())
}

val generatedSrcDir = layout.buildDirectory.dir("generated/sources/buildconfig/kotlin/main")

val generateBuildConfig = tasks.register("generateBuildConfig") {
  val logbackVersion = libs.versions.logback.get()
  val kotestVersion = libs.versions.kotest.get()
  val outputDir = generatedSrcDir
  val pkg = groupId
  inputs.property("logbackVersion", logbackVersion)
  inputs.property("kotestVersion", kotestVersion)
  outputs.dir(outputDir)
  doLast {
    val dir = outputDir.get().asFile.resolve(pkg.replace('.', '/')).apply { mkdirs() }
    dir.resolve("BuildConfig.kt").writeText(
      """
      package $pkg

      internal object BuildConfig {
        const val DEFAULT_LOGBACK_VERSION = "$logbackVersion"
        const val DEFAULT_KOTEST_VERSION = "$kotestVersion"
      }
      """.trimIndent() + "\n",
    )
  }
}

sourceSets.main {
  kotlin.srcDir(generateBuildConfig)
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR)
    exceptionFormat = TestExceptionFormat.FULL
    showStandardStreams = false
  }
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
  create(name) {
    this.id = "$groupId.$id"
    this.implementationClass = "$groupId.$name"
  }
}

gradlePlugin {
  plugins {
    plugin("EnvVarPlugin", "envvar")
    plugin("PublishingPlugin", "publishing")
    plugin("KotlinterPlugin", "kotlinter")
    plugin("TestingPlugin", "testing")
  }
}

dokka {
  moduleName.set(artifactId)
  pluginsConfiguration.html {
    homepageLink.set(githubUrl)
    footerMessage.set(artifactId)
  }
}

mavenPublishing {
  configure(
    com.vanniktech.maven.publish.GradlePlugin(
      javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
      sourcesJar = SourcesJar.Sources(),
    ),
  )
  coordinates(groupId, artifactId, version.toString())

  pom {
    name.set(artifactId)
    description.set("Helpful Gradle plugins for Java and Kotlin projects")
    url.set(githubUrl)
    licenses {
      license {
        name.set("Apache License 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0")
      }
    }
    developers {
      developer {
        id.set("pambrose")
        name.set("Paul Ambrose")
        email.set("paul@pambrose.com")
      }
    }
    scm {
      connection.set("scm:git:git://github.com/pambrose/$artifactId.git")
      developerConnection.set("scm:git:ssh://github.com/pambrose/$artifactId.git")
      url.set(githubUrl)
    }
  }

  publishToMavenCentral(automaticRelease = true)
  signAllPublications()
}

// Skip signing when no GPG key is provided (e.g., local publishing)
tasks.withType<Sign>().configureEach {
  isEnabled = project.findProperty("signingInMemoryKey") != null
}

// Fix implicit dependency: plugin marker publications need explicit dependency on signing
tasks.withType<PublishToMavenRepository>().configureEach {
  dependsOn(tasks.withType<Sign>())
}

tasks.withType<PublishToMavenLocal>().configureEach {
  dependsOn(tasks.withType<Sign>())
}
