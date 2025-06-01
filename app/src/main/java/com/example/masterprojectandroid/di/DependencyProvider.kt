package com.example.masterprojectandroid.di

import android.app.Application
import android.content.Context
import ca.uhn.fhir.context.FhirContext
import com.example.masterprojectandroid.appdatabase.AppDatabase
import com.example.masterprojectandroid.fhir.upload.FhirPatientManager
import com.example.masterprojectandroid.fhir.upload.ObservationUploader
import com.example.masterprojectandroid.healthconnect.services.HealthConnectStatusService
import com.example.masterprojectandroid.healthconnect.data.HealthDataReader
import com.example.masterprojectandroid.healthconnect.services.HealthPermissionManager
import com.example.masterprojectandroid.preferences.PatientPreferences
import com.example.masterprojectandroid.preferences.SyncPreferences
import com.example.masterprojectandroid.viewmodels.*
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory
import com.example.masterprojectandroid.fhir.upload.*
import com.example.masterprojectandroid.fhir.utils.RecordMapper
import com.example.masterprojectandroid.fhir.utils.RecordSyncer
import com.example.masterprojectandroid.healthconnect.services.HealthConnectClientProvider
import com.example.masterprojectandroid.healthconnect.utils.HealthDataFormatter
import com.example.masterprojectandroid.repositories.implementations.*
import com.example.masterprojectandroid.synchronization.*
import com.example.masterprojectandroid.BuildConfig

/**
 * Class responsible for manually wiring up and providing dependencies used throughout the app.
 */
class DependencyProvider(private val context: Context) {

    // Preferences for sync and patient info
    val syncPreferences = SyncPreferences(context.applicationContext)
    val patientPreferences = PatientPreferences(context.applicationContext)

    // Health Connect client and related services
    val healthConnectClient = HealthConnectClient.getOrCreate(context.applicationContext)
    val healthPermissionManager = HealthPermissionManager(healthConnectClient)
    val healthDataFormatter = HealthDataFormatter()
    val healthDataReader = HealthDataReader(healthConnectClient, healthDataFormatter)
    val healthConnectClientProvider = HealthConnectClientProvider()
    val statusService = HealthConnectStatusService(context, healthConnectClientProvider)

    // FHIR client for interacting with the external FHIR server
    private val fhirClient = createFhirClient()

    // Manages FHIR patient resource logic
    private val fhirPatientManager = FhirPatientManager(fhirClient, patientPreferences)

    // Room database and repositories
    private val db = AppDatabase.getDatabase(context.applicationContext)
    internal val historyRecordRepository = HistoryRecordRepository(db.historyRecordDao())
    internal val syncedRecordRepository = SyncedRecordRepository(db.syncedRecordDao())

    // Upload-related utilities
    private val recordMapper = RecordMapper()
    private val recordSyncer = RecordSyncer(syncedRecordRepository)
    private val fhirUploader = FHIRUploader(fhirClient)

    // Handles uploading observations to FHIR
    internal val observationUploader = ObservationUploader(
        recordMapper, fhirUploader, recordSyncer, fhirPatientManager
    )

    // Schedules automatic sync operations
    private val autoSyncWorkerCreator = AutoSyncWorkerScheduler(context)

    // Factory for creating StartupViewModel with required dependencies
    fun startupViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StartupViewModel(
                    syncPreferences = syncPreferences,
                    syncedRecordRepository = syncedRecordRepository,
                    application = context.applicationContext as Application
                ) as T
            }
        }
    }

    // Factory for MainViewModel
    fun mainViewModelFactory(): ViewModelProvider.Factory = factory {
        MainViewModel(
            healthPermissionManager,
            healthDataReader,
            observationUploader,
            historyRecordRepository,
            syncedRecordRepository,
            context.applicationContext as Application
        )
    }

    // Factory for SettingsViewModel
    fun settingsViewModelFactory(): ViewModelProvider.Factory = factory {
        SettingsViewModel(syncPreferences, autoSyncWorkerCreator, healthConnectClient)
    }

    // Factory for PermissionsViewModel
    fun permissionsViewModelFactory(): ViewModelProvider.Factory = factory {
        PermissionsViewModel(statusService, healthPermissionManager)
    }

    // Factory for PatientViewModel
    fun patientViewModelFactory(): ViewModelProvider.Factory = factory {
        PatientViewModel(patientPreferences)
    }

    // Generic inline factory method for ViewModels
    private inline fun <reified T : ViewModel> factory(crossinline create: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T2 : ViewModel> create(modelClass: Class<T2>): T2 = create() as T2
        }
    }

    /**
     * Creates and configures a FHIR client using HAPI FHIR.
     * Sets timeouts and returns a client instance ready for use.
     */
    fun createFhirClient(): IGenericClient {
        val fhirContext = FhirContext.forR4()
        val factory: IRestfulClientFactory = fhirContext.restfulClientFactory

        // Configure timeouts for networking
        factory.socketTimeout = 30_000 // 30 seconds
        factory.connectionRequestTimeout = 10_000 // 10 seconds
        factory.connectTimeout = 10_000 // 10 seconds

        // Create the client with the configured context and base URL
        return fhirContext.newRestfulGenericClient(BuildConfig.FHIR_SERVER_URL)

    }
}
