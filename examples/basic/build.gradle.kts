plugins {
  kotlin("jvm") version "2.0.21"
  id("de.jakobschaefer.htma") version "0.1.8-SNAPSHOT"
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  implementation("de.jakobschaefer.htma:htma-ktor-server:0.1.8-SNAPSHOT")
  implementation("io.ktor:ktor-server-netty:3.0.1")

  implementation("org.apache.logging.log4j:log4j-core:2.24.1")
  implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")
}
