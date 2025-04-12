package de.jakobschaefer.htma.graphql

import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

interface GraphQlService {
  suspend fun execute(request: GraphQlRequest): Map<String, Any>
}

class GraphQlServiceBuilder(
  val schema: String,
) : GraphQlDslBuilder<GraphQlService> {
  private val wiring = RuntimeWiring.newRuntimeWiring()
  private val coroutineScope = CoroutineScope(SupervisorJob())

  @GraphQlDsl
  fun type(typeName: String, spec: GraphQlServiceTypeBuilder.() -> Unit) {
    wiring.type(typeName) { typeBuilder ->
      GraphQlServiceTypeBuilder(coroutineScope, typeBuilder).apply(spec).build()
      typeBuilder
    }
  }

  override fun build(): GraphQlService {
    val schemaParser = SchemaParser()
    val types = schemaParser.parse(schema)
    val schemaGenerator = SchemaGenerator()
    val graphQlSchema = schemaGenerator.makeExecutableSchema(types, wiring.build())
    return GraphQlServiceImpl(graphQlSchema)
  }
}
