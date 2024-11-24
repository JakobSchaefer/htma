plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.htma)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.htma.ktorServer)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)

  implementation(libs.slf4j)
  runtimeOnly(libs.log4j.core)
  runtimeOnly(libs.log4j.slf4j)
}
