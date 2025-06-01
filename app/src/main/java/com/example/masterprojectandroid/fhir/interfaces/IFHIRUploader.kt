package com.example.masterprojectandroid.fhir.interfaces

import org.hl7.fhir.r4.model.Observation

interface IFHIRUploader {
    suspend fun uploadObservations(observations: List<Observation>)
}
