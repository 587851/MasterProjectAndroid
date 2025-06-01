package com.example.masterprojectandroid.appdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.masterprojectandroid.dao.HistoryRecordDao
import com.example.masterprojectandroid.dao.SyncedRecordDao
import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.entities.SyncedRecord

//Database for History and synced records
@Database(entities = [HistoryRecord::class, SyncedRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyRecordDao(): HistoryRecordDao
    abstract fun syncedRecordDao(): SyncedRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                ).fallbackToDestructiveMigration()
                    .build()
            }
        }
    }
}

