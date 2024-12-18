package de.jakobschaefer.graphql

import graphql.language.*

class GqlField(field: FieldDefinition) {
  val fieldName: String = field.name
  val fieldTypeName: String = resolveType(field.type, false)
  val inputs: List<GqlInputField> = field.inputValueDefinitions.map { GqlInputField(it) }

  companion object {
    fun resolveType(type: Type<*>, isNonNull: Boolean): String {
      val nonNullSuffix = if (isNonNull) "" else "?"
      return when (type) {
        is NonNullType -> resolveType(type.type, true)
        is ListType -> "List<${resolveType(type.type, false)}>" + nonNullSuffix
        is TypeName -> {
          val kotlinTypeName =
            when (type.name) {
              "String" -> "String"
              "ID" -> "Uuid"
              "Int" -> "Int"
              "BigInt" -> "Long"
              "Float" -> "Float"
              "BigDecimal" -> "Double"
              "Boolean" -> "Boolean"
              "Currency" -> "Currency"
              else -> "GraphQl${type.name}<T>"
            }
          "$kotlinTypeName$nonNullSuffix"
        }
        else -> TODO("Missing resolver for graphql type $type")
      }
    }
  }
}
