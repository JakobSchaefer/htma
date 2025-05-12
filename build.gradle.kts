import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.kotlin.dsl.add

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.axion)
}

version = scmVersion.version

scmVersion {
  hooks {
    preReleaseHooks.add { context ->
      val packageFile = file("htma-vite-plugin/package.json")
      val packageJson = JsonSlurper().parse(packageFile) as MutableMap<String, String>
      packageJson["version"] = context.releaseVersion

      val updatedJson = JsonOutput.prettyPrint(JsonOutput.toJson(packageJson))
      packageFile.writeText(updatedJson)
      println("package.json version updated to ${context.releaseVersion}")
    }
  }
}
