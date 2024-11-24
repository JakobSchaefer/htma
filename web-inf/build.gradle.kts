plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
}
