package de.jakobschaefer.graphql

import graphql.language.ObjectTypeDefinition

class GqlType(typeDef: ObjectTypeDefinition) {
  val typeName = typeDef.name
  val fields = typeDef.fieldDefinitions.map { GqlField(it) }
}
