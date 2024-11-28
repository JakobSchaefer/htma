package de.jakobschaefer.htma.examples.basic

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Query
import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.graphql.GraphQlEngine
import de.jakobschaefer.htma.routing.htma
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {
  install(Htma) {
    graphqlServices = mapOf(
      "starwars" to StarwarsGraphQlServiceEngine()
    )
  }

  routing {
    htma {
    }
  }
}

class StarwarsGraphQlServiceEngine : GraphQlEngine {
  private val apolloClient = ApolloClient.Builder()
    .serverUrl("https://swapi-graphql.netlify.app/.netlify/functions/index")
    .build()
  override suspend fun <D : Query.Data> query(query: Query<D>): ApolloResponse<D> {
    return apolloClient.query(query).execute()
  }
}

