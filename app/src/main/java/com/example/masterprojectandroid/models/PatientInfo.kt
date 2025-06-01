package com.example.masterprojectandroid.models

/**
 * Represents basic information about a patient, used for identification in FHIR.
 **/
data class PatientInfo(
    val givenName: String,
    val familyName: String,
    val id: String?
)