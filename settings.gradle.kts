pluginManagement {
  includeBuild("htma-gradle-plugin")
  includeBuild("ktor-server-graphql-gradle-plugin")
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "htma"

include(":htma-ktor-server", ":example-basic", ":ktor-server-graphql")
project(":example-basic").projectDir = file("examples/basic")
