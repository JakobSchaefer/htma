plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.axion)
  id("java-library")
  id("maven-publish")
  id("signing")
}

version = scmVersion.version

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

publishing {
  publications {
    create<MavenPublication>("htma") {
      groupId = "io.github.jakobschaefer"
      artifactId = "ktor-server-htma"
      version = project.version.toString()

      pom {
        name = "ktor-server-htma"
        description = "A web framework driven by HTML and powered by Ktor"
        scm {
          url = "https://github.com/JakobSchaefer/ktor-server-htma"
        }
        licenses {
          license {
            name = "MIT License"
            url = "https://raw.githubusercontent.com/JakobSchaefer/ktor-server-htma/refs/heads/main/LICENSE"
          }
        }
        developers {
          developer {
            id = "jakobschaefer"
            name = "Jakob Schäfer"
            email = "mail@jakobschaefer.de"
          }
        }
      }
      from(components["java"])
    }
  }
}

signing {
  // required properties: signing.keyId, signing.password, signing.secretKeyRingFile
  sign(publishing.publications["htma"])
}
