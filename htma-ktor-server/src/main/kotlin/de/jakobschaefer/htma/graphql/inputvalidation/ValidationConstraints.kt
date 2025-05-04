package de.jakobschaefer.htma.graphql.inputvalidation

import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern

fun ValidationBuilder<String>.requireMinLength(length: Int) =
  minLength(length) userContext InputValidationContext("MinLength", mapOf("length" to length))

fun ValidationBuilder<String>.requireMaxLength(length: Int) =
  maxLength(length) userContext InputValidationContext("MaxLength", mapOf("length" to length))

fun ValidationBuilder<String>.requirePattern(pattern: String) =
  pattern(pattern) userContext InputValidationContext("Pattern", mapOf("pattern" to pattern))

fun ValidationBuilder<String>.requireEmail() =
  pattern(".+@.+\\..+") userContext InputValidationContext("Email", emptyMap())
