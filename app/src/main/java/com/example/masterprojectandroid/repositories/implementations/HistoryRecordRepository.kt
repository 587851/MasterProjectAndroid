package com.example.masterprojectandroid.repositories.implementations

import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.dao.HistoryRecordDao
import com.example.masterprojectandroid.repositories.interfaces.IHistoryRecordRepository
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to history records stored in the local database.
 */
class HistoryRecordRepository(private val dao: HistoryRecordDao) : IHistoryRecordRepository {

    /**
     * Retrieves all history records from the database as a list.
     */
    override suspend fun getAllHistoryRecords(): List<HistoryRecord> = dao.getAll()

    /**
     * Retrieves all history records as a reactive Flow that emits updates when the data changes.
     */
    override fun getAllHistoryRecordsFlow(): Flow<List<HistoryRecord>> = dao.getAllRecordsFlow()

    /**
     * Inserts a single history record into the database.
     */
    override suspend fun insertHistoryRecord(record: HistoryRecord) {
        dao.insert(record)
    }

    /**
     * Clears all history records from the database.
     */
    override suspend fun clearAllRecords() {
        dao.clearAll()
    }
}
