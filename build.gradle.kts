plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.axion)
}

version = scmVersion.version
