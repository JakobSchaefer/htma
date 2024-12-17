package de.jakobschaefer.htma.examples.basic

import com.apollographql.apollo.ApolloClient
import de.jakobschaefer.graphql.GraphQl
import de.jakobschaefer.graphql.GraphQlSchemaWiring
import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.graphql.*
import de.jakobschaefer.htma.routing.htma
import de.jakobschaefer.htma.starwars.FilmTitlesQuery
import de.jakobschaefer.htma.starwars.adapter.FilmTitlesQuery_ResponseAdapter
import graphql.schema.idl.RuntimeWiring
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import java.util.*

fun Application.module() {
  var currentName = "World"
  install(GraphQl)

  install(ContentNegotiation) {
    json()
  }

  install(Htma) {
    supportedLocales = listOf(Locale.GERMAN, Locale.ENGLISH)
    graphqlServices = mapOf(
      "graphql" to GraphQlEngineJava(
        schemaResourceFile = "/graphql/schema.graphqls",
        runtimeWiring = RuntimeWiring.newRuntimeWiring()
          .type("Query") { builder ->
            builder.dataFetcher("name") { currentName }
            builder.dataFetcher("greeting") { env ->
              val name = env.getArgument<String>("nameOverride") ?: currentName
              "Hello $name!"
            }
          }
          .build()),
      "starwars" to GraphQlEngineApollo(
        apolloClient = ApolloClient.Builder()
          .serverUrl("https://swapi-graphql.netlify.app/.netlify/functions/index")
          .build()
      )
    )
  }

  routing {
    htma {
    }
  }
}
