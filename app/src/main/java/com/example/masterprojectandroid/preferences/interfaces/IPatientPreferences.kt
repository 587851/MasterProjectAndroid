package com.example.masterprojectandroid.preferences.interfaces

import com.example.masterprojectandroid.models.PatientInfo
import kotlinx.coroutines.flow.Flow

interface IPatientPreferences {
    val patientInfo: Flow<PatientInfo>
    val givenName: Flow<String?>
    val familyName: Flow<String?>

    suspend fun setPatientName(given: String, family: String)
    suspend fun setPatientGiven(given: String)
    suspend fun setPatientFamily(family: String)
    suspend fun setPatientId(id: String)
    suspend fun clearID()
    suspend fun clearAll()
}
