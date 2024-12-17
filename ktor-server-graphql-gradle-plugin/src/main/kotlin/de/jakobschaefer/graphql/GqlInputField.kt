package de.jakobschaefer.graphql

import graphql.language.InputValueDefinition

class GqlInputField(field: InputValueDefinition) {
  val fieldName: String = field.name
  val fieldTypeName: String = GqlField.resolveType(field.type, false)
}
