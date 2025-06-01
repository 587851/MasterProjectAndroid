package com.example.masterprojectandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.masterprojectandroid.entities.HistoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryRecordDao {
    // Retrieves all history records from the table ordered by timestamp in descending order
    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoryRecord>

    // Inserts a new history record into the database
    @Insert
    suspend fun insert(record: HistoryRecord)

    // Returns a Flow that emits the list of history records
    @Query("SELECT * FROM history_records")
    fun getAllRecordsFlow(): Flow<List<HistoryRecord>>

    // Deletes all records from the history_records table
    @Query("DELETE FROM history_records")
    suspend fun clearAll()
}
