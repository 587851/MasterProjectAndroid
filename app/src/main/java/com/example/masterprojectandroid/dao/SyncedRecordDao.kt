package com.example.masterprojectandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.masterprojectandroid.entities.SyncedRecord

@Dao
interface SyncedRecordDao {
    // Retrieves only the IDs that exist in the synced_records table from the provided list
    @Query("SELECT recordId FROM synced_records WHERE recordId IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<String>

    // Inserts a list of SyncedRecord entries into the database
    // If a SyncedRecord with the same ID already exist then, the insert is skipped silently.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<SyncedRecord>)

    // Deletes all records from the synced_records table that are older than the given timestamp
    @Query("DELETE FROM synced_records WHERE measuredAt < :threshold")
    suspend fun deleteOlderThan(threshold: Long)
}

