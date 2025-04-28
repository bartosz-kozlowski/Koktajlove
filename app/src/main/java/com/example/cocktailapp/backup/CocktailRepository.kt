package com.example.cocktailapp

import kotlinx.coroutines.flow.first
/*
class CocktailRepository(private val dao: CocktailDao) {

    val cocktails = dao.getAllCocktails()

    suspend fun getCocktail(id: Int): Cocktail? {
        return dao.getCocktailById(id)
    }

    suspend fun updateFavorite(id: Int, value: Boolean) {
        dao.updateFavorite(id, value)
    }

    // Wstawienie przykładowych danych, jeżeli baza jest pusta
    suspend fun insertDummyData() {
        if (dao.getAllCocktails().first().isEmpty()) {
            /*dao.insertCocktails(
                listOf(
                    Cocktail(
                        name = "Mojito",
                        ingredients = "Rum, Lime, Mint, Sugar, Soda",
                        preparation = "Mix ingredients with ice, top with soda."
                    ),
                    Cocktail(
                        name = "Martini",
                        ingredients = "Gin, Vermouth",
                        preparation = "Stir with ice, strain into glass."
                    ),
                    Cocktail(
                        name = "Piña Colada",
                        ingredients = "Rum, Coconut Cream, Pineapple Juice",
                        preparation = "Blend all ingredients with ice."
                    )
                )
            )*/
        }
    }

}*/