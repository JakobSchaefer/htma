import org.jetbrains.kotlin.codegen.state.md5base64

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.axion)
  id("io.deepmedia.tools.deployer") version "0.15.0"
  id("java-library")
}

version = scmVersion.version
description = "A web framework driven by HTML and powered by Ktor"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(libs.junit.jupiter)

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
}

deployer {
  projectInfo {
    name.set("ktor-server-htma")
    description.set("A web framework driven by HTML and powered by Ktor")
    url.set("https://github.com/JakobSchaefer/ktor-server-htma")
    groupId.set("io.github.jakobschaefer")
    artifactId.set("ktor-server-htma")
    scm {
      fromGithub("ktor-server-htma", "JakobSchaefer")
    }
    license(MIT)
    developer("Jakob Schäfer", "mail@jakobschaefer.de")
  }
  release {
    version.set(project.version.toString())
  }
  content {
    component {
      fromJava()
    }
  }
  signing {
    key.set(secret("gpg.signing.key"))
    password.set(secret("gpg.signing.password"))
  }
  centralPortalSpec {
    allowMavenCentralSync = false
    auth.user.set(secret("mavenCentral.portal.username"))
    auth.password.set(secret("mavenCentral.portal.password"))
  }
}
