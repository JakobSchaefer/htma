pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "ktor-server-htma"

include("lib", "gradle-plugin", "web-inf", "example-basic")

project(":example-basic").projectDir = file("examples/basic")
