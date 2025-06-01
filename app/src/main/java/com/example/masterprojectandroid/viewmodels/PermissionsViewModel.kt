package com.example.masterprojectandroid.viewmodels

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.enums.HealthConnectAvailability
import com.example.masterprojectandroid.healthconnect.services.HealthConnectStatusService
import com.example.masterprojectandroid.healthconnect.services.HealthPermissionManager
import com.example.masterprojectandroid.healthconnect.utils.PERMISSIONS
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val statusService: HealthConnectStatusService,
    private val permissionManager: HealthPermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        viewModelScope.launch {
            val availability = statusService.getAvailability()
            val statusMsg = when (availability) {
                HealthConnectAvailability.INSTALLED -> "✅ Health Connect is installed."
                HealthConnectAvailability.NOT_INSTALLED -> "⚠️ Health Connect is not installed or needs an update."
                HealthConnectAvailability.NOT_SUPPORTED -> "❌ Health Connect is not supported on this device."
            }

            val (read, notRead) = permissionManager.getPermissionStatusGroups()
            _uiState.value = _uiState.value.copy(
                readPermissions = read,
                otherPermissions = notRead,
                healthConnectStatusMessage = statusMsg
            )
        }
    }

    fun requestPermissions() {
        permissionManager.requestPermissions(PERMISSIONS) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowAllGrantedToast)
            }
        }
    }

    fun registerPermissionLauncher(launcher: ActivityResultLauncher<Set<String>>) {
        permissionManager.registerPermissionLauncher(launcher)
    }

    sealed class UiEvent {
        object ShowAllGrantedToast : UiEvent()
    }
}

data class PermissionsUiState(
    val readPermissions: List<Pair<String, Boolean>> = emptyList(),
    val otherPermissions: List<Pair<String, Boolean>> = emptyList(),
    val healthConnectStatusMessage: String? = null
)

