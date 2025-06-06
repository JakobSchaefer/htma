# HTMA - Hypertext Markup Application

A web application framework driven by HTML

## Features

## Installation

You will need a [Ktor](https://ktor.io/) server.
This framework consists of a Ktor server plugin and a Gradle plugin.

```kotlin
// settings.gradle.kts
pluginManagement {
  repositories {
    gradlePluginPortal()
    // HTMA is distributed via maven central
    mavenCentral()
  }
}

// build.gradle.kts
plugins {
  id("de.jakobschaefer.htma") version "0.1.29"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("de.jakobschaefer.htma:htma-ktor-server:0.1.29")
}
```

You can find fully set up projects and feature demos in the [examples](./examples)

