package com.example.masterprojectandroid.repositories.implementations

import com.example.masterprojectandroid.dao.SyncedRecordDao
import com.example.masterprojectandroid.entities.SyncedRecord
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository

/**
 * For managing synced record entries in the local database.
 */
class SyncedRecordRepository(
    private val dao: SyncedRecordDao
) : ISyncedRecordRepository {

    /**
     * Retrieves the list of record IDs that already exist in the synced records table.
     */
    override suspend fun getByIds(ids: List<String>): List<String> {
        return dao.getByIds(ids)
    }

    /**
     * Inserts a list of [SyncedRecord] entries into the database.
     * Records with duplicate primary keys will be ignored.
     */
    override suspend fun insertAll(records: List<SyncedRecord>) {
        dao.insertAll(records)
    }

    /**
     * Deletes records from the database that were measured before
     * the specified threshold.
     */
    override suspend fun deleteOlderThan(threshold: Long) {
        dao.deleteOlderThan(threshold)
    }
}
