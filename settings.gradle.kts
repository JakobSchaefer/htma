pluginManagement {
  includeBuild("htma-gradle-plugin")
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "htma"

include(":htma-ktor-server", ":example-basic", ":htma-vite-plugin")
project(":example-basic").projectDir = file("examples/basic")
