package com.example.masterprojectandroid.fhir.interfaces

import com.example.masterprojectandroid.enums.ObservationType
import org.hl7.fhir.r4.model.Observation
import androidx.health.connect.client.records.*

interface IRecordMapper {
    fun mapToObservation(patientId: String, type: ObservationType, record: Record): List<Observation>
}
