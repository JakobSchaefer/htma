plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.deepmedia.deployer)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.axion)
  alias(libs.plugins.dokka)
}

scmVersion {
  repository {
    directory = project.projectDir.parent
  }
}

group = "de.jakobschaefer.htma"
version = scmVersion.version

val javadocs = tasks.register<Jar>("dokkaJavadocJar") {
  dependsOn(tasks.dokkaJavadoc)
  from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
  archiveClassifier.set("javadoc")
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
  implementation(libs.apollo.gradle.plugin)
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
    url.set("https://github.com/JakobSchaefer/htma")
    artifactId = "htma-gradle-plugin"
    scm {
      fromGithub("JakobSchaefer", "htma")
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
