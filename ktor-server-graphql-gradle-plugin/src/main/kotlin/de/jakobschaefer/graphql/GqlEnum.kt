package de.jakobschaefer.graphql

import graphql.language.EnumTypeDefinition

class GqlEnum(enumDef: EnumTypeDefinition) {
  val typeName = enumDef.name
  val values = enumDef.enumValueDefinitions.map { it.name }
}
