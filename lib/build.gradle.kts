plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.axion)
  alias(libs.plugins.deepmedia.deployer)
  id("java-library")
  id("kotlin")
}

group = "de.jakobschaefer.htma"
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
