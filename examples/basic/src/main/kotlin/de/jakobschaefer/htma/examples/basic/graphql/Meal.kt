package de.jakobschaefer.htma.examples.basic.graphql

data class Meal(
  val id: String,
  val imageUrl: String?,
  val title: String,
  val price: String,
  val ingredients: List<String>
)
