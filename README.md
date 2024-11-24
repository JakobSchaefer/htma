# HTMA - Hypertext Markup Application

A web application framework driven by HTML

## Features

- Isomorphic rendering
- File based routing

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
  id("de.jakobschaefer.htma") version "0.1.4"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("de.jakobschaefer.htma:ktor-server-htma:0.1.4")
}
```

