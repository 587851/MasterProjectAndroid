package com.example.masterprojectandroid.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.masterprojectandroid.models.PatientInfo
import com.example.masterprojectandroid.preferences.interfaces.IPatientPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "patient_prefs")

private val PATIENT_ID_KEY = stringPreferencesKey("patient_id")
val PATIENT_GIVEN_NAME = stringPreferencesKey("patient_given_name")
val PATIENT_FAMILY_NAME = stringPreferencesKey("patient_family_name")

/**
 * Stores and retrieves patient information persistently.
 */
class PatientPreferences(private val context: Context) : IPatientPreferences {

    private val dataStore = context.dataStore

    // Flow that emits the stored patient ID
    val patientId: Flow<String?> = dataStore.data.map { it[PATIENT_ID_KEY] }

    // Flow that emits the stored given name
    override val givenName: Flow<String?> = dataStore.data.map { it[PATIENT_GIVEN_NAME] }

    // Flow that emits the stored family name
    override val familyName: Flow<String?> = dataStore.data.map { it[PATIENT_FAMILY_NAME] }

    // Combines first name, last name, and ID into a single PatientInfo object
    override val patientInfo: Flow<PatientInfo> =
        combine(givenName, familyName, patientId) { given, family, id ->
            PatientInfo(
                givenName = given ?: "test",
                familyName = family ?: "patient",
                id = id
            )
        }

    // Stores both the given and family name
    override suspend fun setPatientName(given: String, family: String) {
        dataStore.edit {
            it[PATIENT_GIVEN_NAME] = given
            it[PATIENT_FAMILY_NAME] = family
        }
    }

    // Stores the given name only
    override suspend fun setPatientGiven(given: String) {
        dataStore.edit {
            it[PATIENT_GIVEN_NAME] = given
        }
    }


    // Stores the family name only
    override suspend fun setPatientFamily(family: String) {
        dataStore.edit {
            it[PATIENT_FAMILY_NAME] = family
        }
    }

    // Stores the patient ID
    override suspend fun setPatientId(id: String) {
        context.dataStore.edit { it[PATIENT_ID_KEY] = id }
    }

    // Clears the stored patient ID
    override suspend fun clearID() {
        dataStore.edit {
            it.remove(PATIENT_ID_KEY)
        }
    }

    // Clears all stored patient information
    override suspend fun clearAll() {
        dataStore.edit {
            it.remove(PATIENT_ID_KEY)
            it.remove(PATIENT_GIVEN_NAME)
            it.remove(PATIENT_FAMILY_NAME)
        }
    }
}
