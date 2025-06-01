package com.example.masterprojectandroid.fhir.utils

import androidx.health.connect.client.records.*
import com.example.masterprojectandroid.entities.SyncedRecord
import com.example.masterprojectandroid.fhir.interfaces.IRecordSyncer
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository
import kotlin.reflect.KClass


/**
 * Responsible for tracking which Health Connect records have already been synced to
 * avoid re-uploading them.
 */
class RecordSyncer(private val syncedRecordRepository: ISyncedRecordRepository) : IRecordSyncer {

    /**
     * A map that associates each Health Connect record class with a lambda that extracts its
     * relevant timestamp.
     */
    private val timeExtractors: Map<KClass<out Record>, (Record) -> Long?> = mapOf(
        BasalBodyTemperatureRecord::class to { (it as BasalBodyTemperatureRecord).time.toEpochMilli() },
        BasalMetabolicRateRecord::class to { (it as BasalMetabolicRateRecord).time.toEpochMilli() },
        BodyFatRecord::class to { (it as BodyFatRecord).time.toEpochMilli() },
        BodyTemperatureRecord::class to { (it as BodyTemperatureRecord).time.toEpochMilli() },
        HeartRateRecord::class to { (it as HeartRateRecord).samples.firstOrNull()?.time?.toEpochMilli() },
        HeartRateVariabilityRmssdRecord::class to { (it as HeartRateVariabilityRmssdRecord).time.toEpochMilli() },
        OxygenSaturationRecord::class to { (it as OxygenSaturationRecord).time.toEpochMilli() },
        RespiratoryRateRecord::class to { (it as RespiratoryRateRecord).time.toEpochMilli() },
        RestingHeartRateRecord::class to { (it as RestingHeartRateRecord).time.toEpochMilli() },
        SkinTemperatureRecord::class to { (it as SkinTemperatureRecord).deltas.firstOrNull()?.time?.toEpochMilli() },
        StepsRecord::class to { (it as StepsRecord).startTime.toEpochMilli() },
        Vo2MaxRecord::class to { (it as Vo2MaxRecord).time.toEpochMilli() },
        WeightRecord::class to { (it as WeightRecord).time.toEpochMilli() }

    )


/**
 * Converts a list of Health Connect records into SyncedRecord entries and saves them
 * in the database to indicate they’ve been uploaded.
 **/
    override suspend fun syncRecords(records: List<Record>): List<SyncedRecord> {
        val ids = records.mapNotNull { record ->
            record.metadata.id?.let { id ->
                val time = timeExtractors[record::class]?.invoke(record)
                time?.let { SyncedRecord(id, it) }
            }
        }
        syncedRecordRepository.insertAll(ids)
        return ids
    }
}