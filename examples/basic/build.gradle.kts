plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.serialization)
  id("de.jakobschaefer.htma")
}

repositories {
  mavenCentral()
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
  implementation(project(":htma-ktor-server"))
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.netty)

  // delete
  implementation(libs.ktor.server.contentNegotiation)
//  implementation("io.ktor:ktor-serialization-kotlinx-json")
  implementation("org.keycloak:keycloak-admin-client:26.0.4")
  // -------

  implementation(libs.slf4j)
  implementation("io.ktor:ktor-server-sessions:3.1.2")
  implementation("io.ktor:ktor-server-core:3.1.1")
  runtimeOnly(libs.log4j.core)
  runtimeOnly(libs.log4j.slf4j)
}
