import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  id(libs.plugins.kotlin.jvm.get().pluginId)
  alias(libs.plugins.deepmedia.deployer)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin.serialization)
}

group = "de.jakobschaefer.htma"
version = rootProject.version
description = "A web framework driven by HTML and powered by Ktor"

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)

  api(libs.ktor.server.core)
  api(libs.ktor.server.sessions)
  implementation(libs.kotlinx.coroutines)
  testImplementation(libs.ktor.server.test)

  implementation(libs.thymeleaf)
  implementation(libs.icu4j)
  implementation(libs.moneta)
  api(libs.graphql.java)
  implementation(libs.gson)
  implementation(libs.betterParse)

  implementation(libs.slf4j)
  testImplementation(libs.log4j.core)
  testImplementation(libs.log4j.slf4j)
  testImplementation(libs.log4j.test)

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
    description = "A web framework driven by HTML and powered by Ktor"
    url = "https://github.com/JakobSchaefer/htma"
    artifactId = "htma-ktor-server"
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
