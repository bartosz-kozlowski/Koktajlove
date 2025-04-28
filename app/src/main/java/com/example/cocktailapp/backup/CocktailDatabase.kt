package com.example.cocktailapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
/*
@Database(entities = [Cocktail::class], version = 3)
abstract class CocktailDatabase : RoomDatabase() {
    abstract fun cocktailDao(): CocktailDao

    companion object {
        @Volatile
        private var INSTANCE: CocktailDatabase? = null

      /* fun getDatabase(context: Context): CocktailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CocktailDatabase::class.java,
                    "cocktail_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }*/
      fun getDatabase(context: Context): CocktailDatabase {
          return INSTANCE ?: synchronized(this) {
              val instance = Room.databaseBuilder(
                  context.applicationContext,
                  CocktailDatabase::class.java,
                  "cocktail_database"
              )
                  .createFromAsset("cocktails_fav.db")
                  //.fallbackToDestructiveMigration()
                  .build()
              INSTANCE = instance
              instance
          }
      }

    }
}*/