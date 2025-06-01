package com.example.masterprojectandroid.fhir.interfaces

import com.example.masterprojectandroid.entities.SyncedRecord
import androidx.health.connect.client.records.Record

interface IRecordSyncer {
    suspend fun syncRecords(records: List<Record>): List<SyncedRecord>
}
