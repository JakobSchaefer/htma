package de.jakobschaefer.htma.graphql

import graphql.schema.idl.TypeRuntimeWiring
import kotlinx.coroutines.CoroutineScope

class GraphQlServiceType {
}

class GraphQlServiceTypeBuilder(
  private val coroutineScope: CoroutineScope,
  private val typeBuilder: TypeRuntimeWiring.Builder
) : GraphQlDslBuilder<GraphQlServiceType> {

  @GraphQlDsl
  fun <T> resolve(fieldName: String, resolver: GraphQlDataResolver<T>) {
    typeBuilder.dataFetcher(fieldName, GraphQlDataFetcher(coroutineScope, resolver))
  }

  override fun build(): GraphQlServiceType {
    return GraphQlServiceType()
  }
}

