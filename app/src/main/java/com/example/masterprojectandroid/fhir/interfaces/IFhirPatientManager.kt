package com.example.masterprojectandroid.fhir.interfaces

interface IFhirPatientManager {
    suspend fun getOrCreatePatientId(): String
}
