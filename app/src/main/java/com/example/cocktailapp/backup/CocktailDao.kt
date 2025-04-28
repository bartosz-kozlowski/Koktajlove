package com.example.cocktailapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
/*
@Dao
interface CocktailDao {
    @Query("SELECT * FROM cocktails")
    fun getAllCocktails(): Flow<List<Cocktail>>

    @Query("SELECT * FROM cocktails WHERE id = :id")
    suspend fun getCocktailById(id: Int): Cocktail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCocktails(cocktails: List<Cocktail>)

    @Query("UPDATE cocktails SET isFavorite = :value WHERE id = :id")
    suspend fun updateFavorite(id: Int, value: Boolean)

}

 */