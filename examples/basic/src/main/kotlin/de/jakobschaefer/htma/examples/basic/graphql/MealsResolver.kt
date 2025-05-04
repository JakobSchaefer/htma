package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment


class MealsResolver(
  val repo: MealsRepository
) : GraphQlDataResolver<List<Meal>> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): List<Meal> {
    return repo.meals
  }

}
