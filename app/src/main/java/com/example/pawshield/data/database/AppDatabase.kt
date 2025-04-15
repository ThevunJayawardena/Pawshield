package com.example.pawshield.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Import TypeConverters annotation
import com.example.pawshield.data.model.DiagnosisHistoryEntry

// Add the TypeConverters annotation here
@Database(entities = [DiagnosisHistoryEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Register your converters class
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pawshield_database"
                )
                    // Consider using proper migrations for production apps instead of fallback
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}