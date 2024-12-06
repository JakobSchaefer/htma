package de.jakobschaefer.htma.graphql.type

import com.apollographql.apollo.api.Optional

data class SetNameOptions(
  val generate: Optional<Boolean?> = Optional.Absent,
)
