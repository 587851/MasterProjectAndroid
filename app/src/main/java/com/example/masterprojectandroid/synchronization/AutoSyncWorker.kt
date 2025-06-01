package com.example.masterprojectandroid.synchronization


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.enums.ObservationType
import com.example.masterprojectandroid.fhir.interfaces.IObservationUploader
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthDataReader
import com.example.masterprojectandroid.preferences.interfaces.ISyncPreferences
import com.example.masterprojectandroid.repositories.interfaces.IHistoryRecordRepository
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * A background worker responsible for automatically synchronizing health data
 * at predefined intervals and storing a history of what was synced.
 */
class AutoSyncWorker(
    private val syncPrefs: ISyncPreferences,
    private val healthDataReader: IHealthDataReader,
    private val observationUploader: IObservationUploader,
    private val syncedRecordRepository: ISyncedRecordRepository,
    private val historyRecordRepository: IHistoryRecordRepository,
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Maps human-readable health data types to their corresponding [ObservationType].
     */
    private val typeMap = mapOf(
        "Basal Body Temperature" to ObservationType.BASAL_BODY_TEMPERATURE,
        "Basal Metabolic Rate" to ObservationType.BASAL_METABOLIC_RATE,
        "Body Fat" to ObservationType.BODY_FAT,
        "Body Temperature" to ObservationType.BODY_TEMPERATURE,
        "Distance" to ObservationType.DISTANCE,
        "Heart Rate" to ObservationType.HEART_RATE,
        "Heart Rate Variability" to ObservationType.HEART_RATE_VARIABILITY,
        "Oxygen Saturation" to ObservationType.OXYGEN_SATURATION,
        "Respiratory Rate" to ObservationType.RESPIRATORY_RATE,
        "Resting Heart Rate" to ObservationType.RESTING_HEART_RATE,
        "Sleep" to ObservationType.SLEEP,
        "Steps" to ObservationType.STEPS,
        "VO2 Max" to ObservationType.VO2_MAX
    )

    /**
     * Executes the background work.
     * Retrieves preferences, filters types to sync, reads records, uploads them,
     * and logs sync history.
     */
    override suspend fun doWork(): Result {
        try {
            Log.i("AutoSyncWorker", "Starting work")
            val frequency = syncPrefs.autoSyncFrequency.first()
            if (frequency == 0) return Result.failure()
            val types = syncPrefs.autoSyncTypes.first()
            if (types.isEmpty()) return Result.failure()

            val allowDuplicates = syncPrefs.allowDuplicates.first()
            val (start, end) = getStartEndFromDefaultPeriod()
            for (type in types) {
                Log.i("AutoSyncWorker", "Starting with $type")
                val obsType = typeMap[type] ?: continue
                val records = healthDataReader.getIntervalRecords(type, start, end)
                val sentCount = observationUploader.sendObservationsFromRecords(
                    type = obsType,
                    records = records,
                    syncedRecordRepository = syncedRecordRepository,
                    allowDuplicates = allowDuplicates
                )
                if (sentCount > 0) {
                    historyRecordRepository.insertHistoryRecord(
                        HistoryRecord(
                            timestamp = Instant.now().toEpochMilli(),
                            dataType = type,
                            dataPointCount = sentCount,
                            periodStart = start.toEpochMilli(),
                            periodEnd = end.toEpochMilli(),
                            source = "Auto-Sync"
                        )
                    )
                }
                Log.i("AutoSyncWorker", "Finished with $type")
            }
        } catch (e: Exception) {
            Log.i("AutoSyncWorker", "Failed work:$e")
            return Result.failure()
        }
        Log.i("AutoSyncWorker", "Finished work")
        return Result.success()
    }

    /**
     * Determines the time window to sync data from, based on the auto-sync frequency setting.
     **/
    private suspend fun getStartEndFromDefaultPeriod(): Pair<Instant, Instant> {
        val frequency = syncPrefs.autoSyncFrequency.first()
        val end = Instant.now()

        val start = when (frequency) {
            1 -> end.minus(30, ChronoUnit.MINUTES)
            2 -> end.minus(2, ChronoUnit.HOURS)
            3 -> end.minus(2, ChronoUnit.DAYS)
            4 -> end.minus(2, ChronoUnit.WEEKS)
            5 -> end.minus(2, ChronoUnit.MONTHS)
            else -> end.minus(2, ChronoUnit.DAYS)
        }
        return start to end
    }

}