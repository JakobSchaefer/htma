package io.github.jakobschaefer.htma.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class HtmaPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.create("hello") {
      doLast {
        println("Hello world!")
      }
    }
  }
}
