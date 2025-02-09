package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.routing.htma
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaParser
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.util.*

fun Application.module() {
  install(Htma) {
    supportedLocales = listOf(Locale.ENGLISH, Locale.GERMAN)
    fallbackLocale = Locale.ENGLISH
  }

  val graphqlTypes = SchemaParser().parse(javaClass.getResourceAsStream("/graphql/schema.graphqls"))

  routing {
    htma {
      graphql(graphqlTypes) {
        RuntimeWiring.newRuntimeWiring()
          .type("Query") { builder ->
            builder.dataFetcher("name") {
              "World"
            }
          }
          .build()
      }
    }
  }
}
