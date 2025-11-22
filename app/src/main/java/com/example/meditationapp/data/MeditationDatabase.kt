package com.example.meditationapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meditationapp.data.dao.MeditationSessionDao
import com.example.meditationapp.data.models.MeditationSession

@Database(
    entities = [MeditationSession::class],
    version = 1,
    exportSchema = false
)
abstract class MeditationDatabase : RoomDatabase() {

    abstract fun meditationSessionDao(): MeditationSessionDao

    companion object {
        @Volatile
        private var INSTANCE: MeditationDatabase? = null

        fun getInstance(context: Context): MeditationDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MeditationDatabase::class.java,
                    "meditation_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
