package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query

interface GraphQlEngine {
  suspend fun <D: Query.Data> query(query: Query<D>): ApolloResponse<D>
  suspend fun <D: Mutation.Data> mutate(mutation: Mutation<D>): ApolloResponse<D>
}
