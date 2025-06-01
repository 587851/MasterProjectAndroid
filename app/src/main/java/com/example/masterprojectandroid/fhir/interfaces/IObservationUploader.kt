package com.example.masterprojectandroid.fhir.interfaces

import androidx.health.connect.client.records.Record
import com.example.masterprojectandroid.enums.ObservationType
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository

interface IObservationUploader {
    suspend fun sendObservationsFromRecords(
        type: ObservationType,
        records: List<Record>,
        syncedRecordRepository: ISyncedRecordRepository,
        allowDuplicates: Boolean
    ): Int
}