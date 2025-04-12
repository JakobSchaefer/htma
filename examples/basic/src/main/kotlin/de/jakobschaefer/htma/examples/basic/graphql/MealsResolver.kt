package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment

data class Meal(
  val title: String,
  val price: String,
  val ingredients: List<String>
)

class MealsResolver : GraphQlDataResolver<List<Meal>> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): List<Meal> {
    return listOf(
      Meal(
        title = "Döner",
        price = "12,99 EUR",
        ingredients = listOf("Fleisch", "Brot")
      ),
      Meal(
        title = "Pommes",
        price = "2,99 EUR",
        ingredients = listOf("Kartoffeln")
      ),
      Meal(
        title = "Salat",
        price = "8,99 EUR",
        ingredients = listOf("Tomaten", "Nüsse")
      ),
    )
  }

}
