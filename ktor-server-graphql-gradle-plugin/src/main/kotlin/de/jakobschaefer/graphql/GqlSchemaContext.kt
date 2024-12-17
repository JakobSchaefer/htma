package de.jakobschaefer.graphql

import graphql.language.EnumTypeDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.TypeDefinitionRegistry

class GqlSchemaContext(schema: TypeDefinitionRegistry) {
  val types: List<GqlType>
  val enums: List<GqlEnum>
  val inputs: List<GqlInput>

  init {
    types =
      schema
        .types()
        .filter { it.value is ObjectTypeDefinition }
        .map { GqlType(it.value as ObjectTypeDefinition) }
    enums =
      schema
        .types()
        .filter { it.value is EnumTypeDefinition }
        .map { GqlEnum(it.value as EnumTypeDefinition) }
    inputs =
      schema
        .types()
        .filter { it.value is InputObjectTypeDefinition }
        .map { GqlInput(it.value as InputObjectTypeDefinition) }
  }
}
