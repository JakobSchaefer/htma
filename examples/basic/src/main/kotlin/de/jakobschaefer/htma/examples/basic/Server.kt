package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.graphql.GraphQl
import de.jakobschaefer.graphql.GraphQlSchemaWiring
import de.jakobschaefer.graphql.typeDefinitions
import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.graphql.*
import de.jakobschaefer.htma.routing.htma
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import java.util.*

fun Application.module() {
  install(GraphQl)

  install(ContentNegotiation) {
    json()
  }

  install(Htma) {
    supportedLocales = listOf(Locale.GERMAN, Locale.ENGLISH)
  }

  routing {
    htma {
      graphql(
        typeDefinitions = application.typeDefinitions,
        contextProvider = { "the-context" },
        runtimeWiring = {
          var currentName = "World"
          GraphQlSchemaWiring<String>().apply {
            typeQuery {
              resolveName { ctx, env -> currentName }
              resolveGreeting { ctx, env, name -> "Hello, ${name ?: currentName}" }
            }
            typeMutation {
              resolveSetName { ctx, env, name, options ->
                currentName = if (options?.generate != null && options.generate) {
                  UUID.randomUUID().toString()
                } else if (name != null) {
                  name
                } else {
                  "World"
                }
                currentName
              }
            }
          }.build()
        })
    }
  }
}
