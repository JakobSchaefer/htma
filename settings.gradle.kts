pluginManagement {
  includeBuild("htma-gradle-plugin")
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "htma"

include(":htma-ktor-server", ":example-basic")
project(":example-basic").projectDir = file("examples/basic")
