plugins {
  alias(libs.plugins.kotlin.jvm)
  id("de.jakobschaefer.htma") version "0.1.4"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("de.jakobschaefer.htma:ktor-server-htma:0.1.4")
}
