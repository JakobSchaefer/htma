plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.deepmedia.deployer)
  alias(libs.plugins.dokka)
}

group = "de.jakobschaefer.htma"
version = rootProject.version

val javadocs = tasks.register<Jar>("dokkaJavadocJar") {
  dependsOn(tasks.dokkaJavadoc)
  from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
  archiveClassifier.set("javadoc")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":web-inf"))
}

gradlePlugin {
  plugins {
    create("htma") {
      id = "de.jakobschaefer.htma"
      implementationClass = "de.jakobschaefer.htma.gradle.HtmaPlugin"
    }
  }
}

deployer {
  content {
    gradlePluginComponents {
      kotlinSources()
      docs(javadocs)
    }
  }

  projectInfo {
    description = "A web framework driven by HTML and powered by Ktor"
    url.set("https://github.com/JakobSchaefer/ktor-server-htma")
    artifactId = "ktor-server-htma-gradle-plugin"
    scm {
      fromGithub("JakobSchaefer", "ktor-server-htma")
    }
    license(MIT)
    developer("JakobSchaefer", "mail@jakobschaefer.de")
  }

  localSpec {}

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
