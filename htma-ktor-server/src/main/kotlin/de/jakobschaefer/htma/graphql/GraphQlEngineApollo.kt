package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query

class GraphQlEngineApollo(private val apolloClient: ApolloClient) : GraphQlEngine {
  override suspend fun <D : Query.Data> query(query: Query<D>): ApolloResponse<D> {
    return apolloClient.query(query).execute()
  }

  override suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>): ApolloResponse<D> {
    return apolloClient.mutation(mutation).execute()
  }
}

