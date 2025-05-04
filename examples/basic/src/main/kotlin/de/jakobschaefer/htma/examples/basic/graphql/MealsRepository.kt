package de.jakobschaefer.htma.examples.basic.graphql

class MealsRepository {
  var meals = mutableListOf(
    Meal(
      id = "1",
      title = "Döner",
      imageUrl = null,
      price = "12,99 EUR",
      ingredients = listOf("Fleisch", "Brot")
    ),
    Meal(
      id = "2",
      title = "Pommes",
      imageUrl = null,
      price = "2,99 EUR",
      ingredients = listOf("Kartoffeln")
    ),
    Meal(
      id = "3",
      title = "Salat",
      imageUrl = null,
      price = "8,99 EUR",
      ingredients = listOf("Tomaten", "Nüsse")
    ),
  )

  fun addMeal(meal: Meal) {
    meals.add(meal)
  }

  fun deleteMeal(id: String) {
    meals = meals.filter { it.id != id }.toMutableList()
  }

  fun listMeals(): List<Meal> {
    return meals
  }
}
