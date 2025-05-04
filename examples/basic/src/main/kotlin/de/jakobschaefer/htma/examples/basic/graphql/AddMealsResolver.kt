package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment
import java.util.UUID

class AddMealsResolver(
  val repo: MealsRepository
) : GraphQlDataResolver<Meal> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): Meal {
    val request = env.arguments["request"] as Map<String, Any>
    val meal = Meal(
      id = UUID.randomUUID().toString(),
      title = request["title"] as String,
      imageUrl = request["image"] as String?,
      price = request["price"] as String,
      ingredients = request["ingredients"] as List<String>
    )
    repo.addMeal(meal)
    return meal
  }
}

class AddMealRequest(
  val title: String,
  val imageUrl: String,
  val price: String,
  val ingredients: List<String>
)
