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

group = "de.jakobschaefer.graphql"
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
  implementation(libs.graphql.java)
  implementation(libs.handlebars.java)
}

gradlePlugin {
  plugins {
    create("htma") {
      id = "de.jakobschaefer.graphql"
      implementationClass = "de.jakobschaefer.graphql.GraphQlPlugin"
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
    description = "Gradle plugin for ktor-server-graphql"
    url.set("https://github.com/JakobSchaefer/htma")
    artifactId = "ktor-server-graphql-gradle-plugin"
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
