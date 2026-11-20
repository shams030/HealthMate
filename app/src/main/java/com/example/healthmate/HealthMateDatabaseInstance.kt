package com.example.healthmate

import android.content.Context
import androidx.room.Room

object HealthMateDatabaseInstance {

    fun getDatabase(context: Context): HealthMateDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HealthMateDatabase::class.java,
            "healthmate_database"
        ).build()
    }
}