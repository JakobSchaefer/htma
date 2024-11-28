import com.apollographql.apollo.gradle.api.ApolloExtension

plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  alias(libs.plugins.ktor)
  id("de.jakobschaefer.htma")
}

repositories {
  mavenCentral()
}

apollo {
  service("graphql") {
    packageName.set("de.jakobschaefer.htma.graphql")
    schemaFiles.from("src/main/resources/graphql/schema.graphqls")
    srcDir("web")
    includes.add("**/*.graphql")
    generateApolloMetadata.set(true)
  }
  service("starwars") {
    packageName.set("de.jakobschaefer.htma.starwars")
    schemaFiles.from("src/main/graphql/starwars/schema.graphqls")
    srcDir("starwars")
    includes.add("**/*.graphql")
    introspection {
      endpointUrl.set("https://swapi-graphql.netlify.app/.netlify/functions/index")
      schemaFile.set(file("src/main/graphql/starwars/schema.graphqls"))
    }
  }
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
  implementation(project(":htma-ktor-server"))
  implementation(libs.ktor.server.netty)

  implementation(libs.apollo.runtime)

  implementation(libs.slf4j)
  runtimeOnly(libs.log4j.core)
  runtimeOnly(libs.log4j.slf4j)
}
