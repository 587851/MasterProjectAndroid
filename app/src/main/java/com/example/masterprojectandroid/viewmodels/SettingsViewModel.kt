package com.example.masterprojectandroid.viewmodels

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.preferences.interfaces.ISyncPreferences
import com.example.masterprojectandroid.synchronization.AutoSyncWorkerScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the settings related to sync behavior,
 * including auto-sync frequency, allowed data types, cleanup preferences, and duplication policy.
 * Used by SettingsScreen
 */
class SettingsViewModel(
    private val syncPreferences: ISyncPreferences,
    private val autoSyncWorkerCreator: AutoSyncWorkerScheduler,
    private val healthConnectClient: HealthConnectClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        object ResetFrequencyToNever : UiEvent()
    }

    init {
        observePreferences()
    }

    /**
     * Observes sync preferences and updates UI state accordingly.
     */
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                syncPreferences.allowDuplicates,
                syncPreferences.cleanupAgeDays,
                syncPreferences.autoSyncFrequency,
                syncPreferences.autoSyncTypes
            ) { allowDup, cleanupDays, freq, types ->
                SettingsUiState(
                    allowDuplicates = allowDup,
                    cleanupAgeDays = cleanupDays,
                    autoSyncFrequency = freq,
                    autoSyncTypes = types
                )
            }.collect { _uiState.value = it }
        }
    }

    /**
     * Updates whether duplicate records are allowed to be uploaded.
     */
    fun setAllowDuplicates(value: Boolean) {
        viewModelScope.launch {
            syncPreferences.setAllowDuplicates(value)
        }
    }

    /**
     * Sets the age threshold (in days) for automatically deleting synced records.
     */
    fun setCleanupAgeDays(days: Int) {
        viewModelScope.launch {
            syncPreferences.setCleanupAgeDays(days)
        }
    }

    /**
     * Sets the frequency for auto-sync and schedules a worker if the appropriate
     * background permission is granted and the feature is available.
     * If not, notifies the UI and resets frequency to "never".
     */
    fun setAutoSyncFrequency(value: Int) {
        viewModelScope.launch {
            syncPreferences.setAutoSyncFrequency(value)

            if (value == 0) return@launch

            val featureStatus = healthConnectClient.features.getFeatureStatus(
                HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
            )
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val backgroundPermission = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"

            val hasPermission = backgroundPermission in grantedPermissions
            if (featureStatus == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE && hasPermission) {
                autoSyncWorkerCreator.scheduleAutoSyncWorker(value)
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Background read permission is missing."))
                _uiEvent.send(UiEvent.ResetFrequencyToNever)
                syncPreferences.setAutoSyncFrequency(0)
            }
        }
    }

    /**
     * Updates the types of health data to include in auto-sync.
     */
    fun setAutoSyncTypes(types: Set<String>) {
        viewModelScope.launch {
            syncPreferences.setAutoSyncTypes(types)
        }
    }
}

/**
 * UI representation of settings stored in preferences.
 */
data class SettingsUiState(
    val allowDuplicates: Boolean = false,
    val cleanupAgeDays: Int = 0,
    val autoSyncFrequency: Int = 0,
    val autoSyncTypes: Set<String> = emptySet()
)
