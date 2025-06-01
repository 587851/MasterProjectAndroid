package com.example.masterprojectandroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.models.PatientInfo
import com.example.masterprojectandroid.preferences.interfaces.IPatientPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing patient information such as name and ID.
 * Interacts with patient preferences to persist and retrieve data.
 * Used by: SettingsScreen
 */
class PatientViewModel(
    private val patientPreferences: IPatientPreferences
) : ViewModel() {

    /**
     * Exposes the current patient information as a StateFlow.
     * Defaults to a test patient if no name is set.
     */
    val patientInfo: StateFlow<PatientInfo> = patientPreferences.patientInfo
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PatientInfo("test", "patient", null)
        )

    /**
     * Initializes patient preferences with default values if unset.
     */
    init {
        viewModelScope.launch {
            val currentGiven = patientPreferences.givenName.first()
            val currentFamily = patientPreferences.familyName.first()

            if (currentGiven == null) {
                patientPreferences.setPatientGiven("Test")
            }
            if (currentFamily == null) {
                patientPreferences.setPatientFamily("Patient")
            }
        }
    }

    /**
     * Updates the patient's given and family name in preferences.
     * Also clears the patient ID.
     **/
    fun updateName(given: String, family: String) {
        viewModelScope.launch {
            patientPreferences.setPatientName(given, family)
            patientPreferences.clearID()
        }
    }
}



