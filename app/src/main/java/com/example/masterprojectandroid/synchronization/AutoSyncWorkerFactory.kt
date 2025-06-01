package com.example.masterprojectandroid.synchronization

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.masterprojectandroid.fhir.interfaces.IObservationUploader
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthDataReader
import com.example.masterprojectandroid.preferences.interfaces.ISyncPreferences
import com.example.masterprojectandroid.repositories.interfaces.IHistoryRecordRepository
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository


/**
 * A custom [WorkerFactory] used to create instances of [AutoSyncWorker] with
 * its required dependencies that cannot be passed through the default constructor.
 */
class AutoSyncWorkerFactory(
    private val syncPrefs: ISyncPreferences,
    private val healthDataReader: IHealthDataReader,
    private val observationUploader: IObservationUploader,
    private val syncedRecordRepository: ISyncedRecordRepository,
    private val historyRecordRepository: IHistoryRecordRepository
) : WorkerFactory() {

    /**
     * Creates a [ListenableWorker] instance for the given worker class name.
     * Only creates an instance of [AutoSyncWorker]; returns null for other classes.
     **/
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.i("AutoSyncWorkerFactory", "Creating worker")
        return when (workerClassName) {
            AutoSyncWorker::class.java.name -> AutoSyncWorker(
                syncPrefs,
                healthDataReader,
                observationUploader,
                syncedRecordRepository,
                historyRecordRepository,
                appContext,
                workerParameters
            )
            else -> null
        }
    }

}

