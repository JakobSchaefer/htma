pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "ktor-server-htma"

include("lib", "gradle-plugin", "web-inf")
