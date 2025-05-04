package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment

class DeleteMealResolver(
  val repo: MealsRepository
) : GraphQlDataResolver<Boolean> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): Boolean {
    val id = env.arguments["id"] as String
    repo.deleteMeal(id)
    return true
  }
}
