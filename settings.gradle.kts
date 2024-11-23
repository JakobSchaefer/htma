pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "ktor-server-htma"

include("lib", "gradle-plugin", "example-basic")

project(":example-basic").projectDir = file("examples/basic")
