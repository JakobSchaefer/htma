package de.jakobschaefer.htma.examples.basic

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Query
import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.graphql.GraphQlEngine
import de.jakobschaefer.htma.graphql.GraphQlEngineApollo
import de.jakobschaefer.htma.graphql.GraphQlEngineJava
import de.jakobschaefer.htma.routing.htma
import graphql.schema.idl.RuntimeWiring
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {
  install(Htma) {
    graphqlServices = mapOf(
      "graphql" to GraphQlEngineJava(
        schemaResourceFile = "/graphql/schema.graphqls",
        runtimeWiring = RuntimeWiring.newRuntimeWiring()
          .type("Query") { builder ->
            builder.dataFetcher("hello") { env ->
              val name = env.getArgument<String?>("name") ?: "World"
              "Hello, $name!"
            }
          }
          .build()
        ),
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
