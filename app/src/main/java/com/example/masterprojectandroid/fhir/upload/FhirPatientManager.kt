package com.example.masterprojectandroid.fhir.upload

import ca.uhn.fhir.rest.client.api.IGenericClient
import com.example.masterprojectandroid.fhir.interfaces.IFhirPatientManager
import com.example.masterprojectandroid.preferences.PatientPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType

/**
 * Implementation of IFhirPatientManager responsible for managing FHIR Patient resources.
 * It ensures that a patient exists on the FHIR server and can create a new one if needed.
 */
class FhirPatientManager(
    private val fhirClient: IGenericClient,
    private val patientPreferences: PatientPreferences
) : IFhirPatientManager {

    /**
     * Retrieves the existing patient ID from preferences if valid,
     * otherwise creates a new patient on the FHIR server and stores the new ID.
     */
    override suspend fun getOrCreatePatientId(): String {
        val existingId = patientPreferences.patientId.first()
        val existingFirstName = patientPreferences.givenName.first()
        val existingLastName = patientPreferences.familyName.first()

        // If a patient ID is already stored, verify if it still exists on the server
        if (existingId != null) {
            val exists = withContext(Dispatchers.IO) {
                try {
                    fhirClient.read()
                        .resource(Patient::class.java)
                        .withId(existingId)
                        .execute()
                    true
                } catch (e: Exception) {
                    false
                }
            }

            // If the patient exists on the server, return the ID
            // Otherwise, clear saved preferences to prepare for new patient creation
            if (exists) return existingId else patientPreferences.clearAll()
        }

        // Create a new FHIR Patient resource using the stored name information
        val patient = Patient().apply {
            addName().apply {
                this.given = listOf(StringType(existingFirstName))
                this.family = existingLastName
            }
        }

        // Send a create request to the FHIR server to store the new patient
        val outcome = withContext(Dispatchers.IO) {
            fhirClient.create().resource(patient).execute()
        }

        // Extract the generated ID from the server's response and save it locally
        val newId = outcome.id.idPart
        patientPreferences.setPatientId(newId)
        return newId
    }
}