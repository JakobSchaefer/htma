import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
  alias(libs.plugins.axion)
  alias(libs.plugins.node)
}

version = scmVersion.version

scmVersion {
  hooks {
    preReleaseHooks.add { context ->
      val packageFile = file("package.json")
      val packageJson = JsonSlurper().parse(packageFile) as MutableMap<String, String>
      packageJson["version"] = context.releaseVersion

      val updatedJson = JsonOutput.prettyPrint(JsonOutput.toJson(packageJson))
      packageFile.writeText(updatedJson)
      println("package.json version updated to ${context.releaseVersion}")
    }
  }
}
