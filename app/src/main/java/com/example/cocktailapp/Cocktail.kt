package com.example.cocktailapp

import com.google.firebase.firestore.PropertyName

data class Cocktail(
    val id: String = "", // ID dokumentu Firestore
    val name: String = "",
    val ingredients: String = "",
    val preparation: String = "",
    val imageUrl: String = "",
    @get:PropertyName("alcoholic")
    @set:PropertyName("alcoholic")
    var isAlcoholic: Boolean = false,
    //val isAlcoholic: Boolean = false,
    @get:PropertyName("favorite")
    @set:PropertyName("favorite")
    var isFavorite: Boolean = false,
    @get:PropertyName("preparationTime")
    @set:PropertyName("preparationTime")
    var preparationTime: Int = 0
)
