package de.jakobschaefer.graphql

import graphql.language.InputObjectTypeDefinition

class GqlInput(inputDef: InputObjectTypeDefinition) {
  val typeName = inputDef.name
  val fields = inputDef.inputValueDefinitions.map { GqlInputField(it) }
}
