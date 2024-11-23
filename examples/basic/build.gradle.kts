plugins {
  alias(libs.plugins.kotlin.jvm)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.github.jakobschaefer:ktor-server-htma:0.1.1")
}
