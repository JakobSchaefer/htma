package de.jakobschaefer.htma.graphql.inputvalidation

data class InputValidationContext(val type: String, val params: Map<String, Any>)
