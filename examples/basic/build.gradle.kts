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

  implementation("com.h2database:h2:2.3.232")

  implementation(libs.ktor.server.auth)

  implementation(libs.slf4j)
  runtimeOnly(libs.log4j.core)
  runtimeOnly(libs.log4j.slf4j)
}
