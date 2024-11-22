plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.axion)
  id("java-library")
  id("maven-publish")
}

version = scmVersion.version

publishing {
  repositories {
    maven {
      name = "GitHub"
      url = uri("https://maven.pkg.github.com/JakobSchaefer/ktor-server-htma")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
  publications {
    create<MavenPublication>("maven") {
      pom {
        name = "ktor-server-htma"
        description = "A web framework driven by HTML and powered by Ktor"
        licenses {
          license {
            name = "MIT License"
            url = "https://raw.githubusercontent.com/JakobSchaefer/ktor-server-htma/refs/heads/main/LICENSE"
          }
        }
        developers {
          developer {
            name = "Jakob Schäfer"
            email = "mail@jakobschaefer.de"
          }
        }
      }
      groupId = "de.jakobschaefer.htma"
      artifactId = "ktor-server-htma"
      version = project.version.toString()

      from(components["java"])
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(libs.junit.jupiter)

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}
