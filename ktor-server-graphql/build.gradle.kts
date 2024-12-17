import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  id(libs.plugins.kotlin.jvm.get().pluginId)
  alias(libs.plugins.deepmedia.deployer)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin.serialization)
}

group = "de.jakobschaefer.graphql"
version = rootProject.version
description = "Ktor graphql server plugin"

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  api(libs.ktor.server.core)
  testImplementation(libs.ktor.server.test)

  api(libs.graphql.java)
  implementation(libs.gson)

  testImplementation(libs.jsoup)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
}

java {
  withSourcesJar()
  withJavadocJar()
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
  testLogging {
    events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showExceptions = true
    showCauses = true
  }
}

deployer {
  content {
    component {
      fromJava()
    }
  }

  projectInfo {
    description = "Ktor graphql server plugin"
    url = "https://github.com/JakobSchaefer/htma"
    artifactId = "ktor-server-graphql"
    scm {
      fromGithub("JakobSchaefer", "htma")
    }
    license(MIT)
    developer("JakobSchaefer", "mail@jakobschaefer.de")
  }

  localSpec { }

  centralPortalSpec {
    allowMavenCentralSync = false
    auth.user.set(secret("mavenCentral.portal.username"))
    auth.password.set(secret("mavenCentral.portal.password"))
  }

  signing {
    key.set(secret("gpg.signing.key"))
    password.set(secret("gpg.signing.password"))
  }
}
