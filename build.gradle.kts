import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.plugins.signing.Sign
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.ben.manes.versions)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
}

version = findProperty("overrideVersion")?.toString() ?: "1.0.12"
group = "com.pambrose"

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
  testLogging {
    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
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
    plugin("KotlinterPlugin", "kotlinter")
    plugin("TestingPlugin", "testing")
  }
}

mavenPublishing {
  configure(
    com.vanniktech.maven.publish.GradlePlugin(
      javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
      sourcesJar = true,
    ),
  )
  coordinates("com.pambrose", "pambrose-gradle-plugins", version.toString())

  pom {
    name.set("pambrose-gradle-plugins")
    description.set("Helpful Gradle plugins for Java and Kotlin projects")
    url.set("https://github.com/pambrose/pambrose-gradle-plugins")
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
      connection.set("scm:git:git://github.com/pambrose/pambrose-gradle-plugins.git")
      developerConnection.set("scm:git:ssh://github.com/pambrose/pambrose-gradle-plugins.git")
      url.set("https://github.com/pambrose/pambrose-gradle-plugins")
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
