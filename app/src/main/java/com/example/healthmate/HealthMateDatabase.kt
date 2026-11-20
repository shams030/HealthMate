package com.example.healthmate

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HealthRecord::class],
    version = 1,
    exportSchema = false
)
abstract class HealthMateDatabase : RoomDatabase() {

    abstract fun healthRecordDao(): HealthRecordDao
}