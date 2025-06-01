package com.example.masterprojectandroid.fhir.upload

import android.util.Log
import androidx.health.connect.client.records.*
import com.example.masterprojectandroid.enums.ObservationType
import com.example.masterprojectandroid.fhir.interfaces.IFHIRUploader
import com.example.masterprojectandroid.fhir.interfaces.IFhirPatientManager
import com.example.masterprojectandroid.fhir.interfaces.IObservationUploader
import com.example.masterprojectandroid.fhir.interfaces.IRecordMapper
import com.example.masterprojectandroid.fhir.interfaces.IRecordSyncer
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository


/**
 * Handles converting Health Connect records into FHIR Observations
 * and uploading them to a FHIR server. Sync uploaded records
 */
class ObservationUploader(
    private val recordMapper: IRecordMapper,
    private val fhirUploader: IFHIRUploader,
    private val recordSyncer: IRecordSyncer,
    private val patientManager: IFhirPatientManager
) : IObservationUploader {

    /**
     * Converts and uploads a list of Health Connect records to FHIR Observations.
     **/
    override suspend fun sendObservationsFromRecords(
        type: ObservationType,
        records: List<Record>,
        syncedRecordRepository: ISyncedRecordRepository,
        allowDuplicates: Boolean
    ): Int {
        val patientId = patientManager.getOrCreatePatientId()

        // Filter out records th    at have already been synced, unless duplicates are allowed
        val newRecords = if (!allowDuplicates) {
            val allIds = records.mapNotNull { it.metadata.id }
            val syncedIds = syncedRecordRepository.getByIds(allIds)
            records.filter { it.metadata.id?.let { id -> id !in syncedIds } ?: false }
        } else records

        // Convert the remaining records into FHIR Observation resources
        val observations = newRecords.flatMap { record ->
            recordMapper.mapToObservation(patientId, type, record)
        }

        val chunkSize = 500
        val chunks = observations.chunked(chunkSize)
        val recordChunks = newRecords.chunked(chunkSize)
        //Observations are uploaded in chunks to avoid exceeding server or payload limits.
        for ((i, obsChunk) in chunks.withIndex()) {
            try {
                fhirUploader.uploadObservations(obsChunk)
                recordSyncer.syncRecords(recordChunks[i])
            } catch (e: Exception) {
                Log.e("ObservationUploader", "Failed to upload chunk $i: ${e.message}", e)
                break
            }
        }
        return observations.size
    }

}

