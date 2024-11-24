import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.deepmedia.deployer)
  alias(libs.plugins.dokka)
}

group = "de.jakobschaefer.htma"
version = rootProject.version
description = "A web framework driven by HTML and powered by Ktor"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":web-inf"))

  api(libs.ktor.server.core)
  testImplementation(libs.ktor.server.test)

  implementation(libs.slf4j)
  testImplementation(libs.log4j.core)
  testImplementation(libs.log4j.slf4j)
  testImplementation(libs.log4j.test)

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
    description = "A web framework driven by HTML and powered by Ktor"
    url = "https://github.com/JakobSchaefer/ktor-server-htma"
    artifactId = "ktor-server-htma"
    scm {
      fromGithub("JakobSchaefer", "ktor-server-htma")
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
