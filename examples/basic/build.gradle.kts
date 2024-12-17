plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.serialization)
  id("de.jakobschaefer.htma")
  id("de.jakobschaefer.graphql")
}

repositories {
  mavenCentral()
}

graphql {
  packageName = "de.jakobschaefer.htma.graphql"
  schemaFile = layout.projectDirectory.file("src/main/resources/graphql/schema.graphqls")
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
  implementation(project(":ktor-server-graphql"))
  implementation(project(":htma-ktor-server"))
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.contentNegotiation)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.apollo.runtime)

  implementation(libs.slf4j)
  runtimeOnly(libs.log4j.core)
  runtimeOnly(libs.log4j.slf4j)
}
