package com.example.masterprojectandroid

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.masterprojectandroid.di.DependencyProvider
import com.example.masterprojectandroid.synchronization.AutoSyncWorkerFactory

/**
 * Main application class where global initialization takes place.
 * Implements Configuration.Provider to allow custom WorkerFactory for WorkManager.
 */
class MainApplication : Application(), Configuration.Provider {

    lateinit var dependencyProvider: DependencyProvider
        private set

    /**
     * Called when the application is starting, before any other application objects have been created.
     */
    override fun onCreate() {
        super.onCreate()

        dependencyProvider = DependencyProvider(this)

        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(
                    AutoSyncWorkerFactory(
                        dependencyProvider.syncPreferences,
                        dependencyProvider.healthDataReader,
                        dependencyProvider.observationUploader,
                        dependencyProvider.syncedRecordRepository,
                        dependencyProvider.historyRecordRepository
                    )
                )
                .build()
        )
    }

    /**
     * Provides custom WorkManager configuration using the same AutoSyncWorkerFactory.
     * This ensures the WorkManager uses injected dependencies for background work.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                AutoSyncWorkerFactory(
                    dependencyProvider.syncPreferences,
                    dependencyProvider.healthDataReader,
                    dependencyProvider.observationUploader,
                    dependencyProvider.syncedRecordRepository,
                    dependencyProvider.historyRecordRepository
                )
            )
            .build()
}
