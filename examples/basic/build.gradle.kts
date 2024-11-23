plugins {
  alias(libs.plugins.kotlin.jvm)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("de.jakobschaefer.htma:ktor-server-htma:0.1.3")
}
