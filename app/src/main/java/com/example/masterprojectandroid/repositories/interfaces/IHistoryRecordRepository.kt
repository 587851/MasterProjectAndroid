package com.example.masterprojectandroid.repositories.interfaces

import com.example.masterprojectandroid.entities.HistoryRecord
import kotlinx.coroutines.flow.Flow

interface IHistoryRecordRepository {
    suspend fun getAllHistoryRecords(): List<HistoryRecord>
    suspend fun insertHistoryRecord(record: HistoryRecord)
    fun getAllHistoryRecordsFlow(): Flow<List<HistoryRecord>>
    suspend fun clearAllRecords()
}