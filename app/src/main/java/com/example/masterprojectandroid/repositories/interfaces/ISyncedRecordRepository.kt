package com.example.masterprojectandroid.repositories.interfaces

import com.example.masterprojectandroid.entities.SyncedRecord

interface ISyncedRecordRepository {
    suspend fun getByIds(ids: List<String>): List<String>
    suspend fun insertAll(recordIds: List<SyncedRecord>)
    suspend fun deleteOlderThan(threshold: Long)
}