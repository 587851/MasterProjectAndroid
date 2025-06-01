package com.example.masterprojectandroid.fhir.utils

import android.util.Log
import androidx.health.connect.client.records.*
import com.example.masterprojectandroid.enums.ObservationType
import com.example.masterprojectandroid.fhir.interfaces.IRecordMapper
import org.hl7.fhir.r4.model.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Maps Health Connect records to corresponding FHIR Observation resources.
 */
class RecordMapper : IRecordMapper {

    companion object {
        const val LOINC_SYSTEM = "http://loinc.org"
        const val CATEGORY_SYSTEM = "http://terminology.hl7.org/CodeSystem/observation-category"
        const val UNIT_SYSTEM = "http://unitsofmeasure.org"
    }

    private lateinit var patientId: String
    private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)

    /**
     * Maps a specific ObservationType to a function that converts a Health Connect Record
     * to a list of FHIR Observation resources.
     */
    private val recordTypeToObservationTypeMapper: Map<ObservationType, (Record) -> List<Observation>> = mapOf(
        ObservationType.BASAL_BODY_TEMPERATURE to { record ->
            (record as? BasalBodyTemperatureRecord)?.let {
                listOf(createVital(patientId, "8310-5", "Basal body temperature", "°C", it.temperature.inCelsius, it.time))
            } ?: emptyList()
        },
        ObservationType.BASAL_METABOLIC_RATE to { record ->
            (record as? BasalMetabolicRateRecord)?.let {
                listOf(createVital(patientId, "69429-9", "Basal metabolic rate", "kcal/day", it.basalMetabolicRate.inKilocaloriesPerDay, it.time))
            } ?: emptyList()
        },
        ObservationType.BODY_FAT to { record ->
            (record as? BodyFatRecord)?.let {
                listOf(createVital(patientId, "41982-0", "Body fat", "%", it.percentage.value, it.time))
            } ?: emptyList()
        },
        ObservationType.BODY_TEMPERATURE to { record ->
            (record as? BodyTemperatureRecord)?.let {
                listOf(createVital(patientId, "8310-5", "Body temperature", "°C", it.temperature.inCelsius, it.time))
            } ?: emptyList()
        },
        ObservationType.DISTANCE to { record ->
            (record as? DistanceRecord)?.let {
                listOf(createActivity(patientId, "55430-3", "Distance traveled", "meters", it.distance.inMeters, it.startTime, it.endTime))
            } ?: emptyList()
        },
        ObservationType.HEART_RATE to { record ->
            (record as? HeartRateRecord)?.samples?.map {
                createVital(patientId, "8867-4", "Heart rate", "beats/minute", it.beatsPerMinute.toDouble(), it.time)
            } ?: emptyList()
        },
        ObservationType.HEART_RATE_VARIABILITY to { record ->
            (record as? HeartRateVariabilityRmssdRecord)?.let {
                listOf(createVital(patientId, "80404-7", "Heart rate variability", "ms", it.heartRateVariabilityMillis, it.time))
            } ?: emptyList()
        },
        ObservationType.OXYGEN_SATURATION to { record ->
            (record as? OxygenSaturationRecord)?.let {
                listOf(createVital(patientId, "59408-5", "Oxygen saturation", "%", it.percentage.value, it.time))
            } ?: emptyList()
        },
        ObservationType.RESPIRATORY_RATE to { record ->
            (record as? RespiratoryRateRecord)?.let {
                listOf(createVital(patientId, "9279-1", "Respiratory rate", "breaths/min", it.rate, it.time))
            } ?: emptyList()
        },
        ObservationType.RESTING_HEART_RATE to { record ->
            (record as? RestingHeartRateRecord)?.let {
                listOf(createVital(patientId, "40443-4", "Resting heart rate", "beats/minute", it.beatsPerMinute.toDouble(), it.time))
            } ?: emptyList()
        },
        ObservationType.SLEEP to { record ->
            (record as? SleepSessionRecord)?.stages?.map {
                createActivity(patientId, "93832-4", "Sleep session", "sleep stage", it.stage.toDouble(), it.startTime, it.endTime)
            } ?: emptyList()
        },
        ObservationType.STEPS to { record ->
            (record as? StepsRecord)?.let {
                listOf(createActivity(patientId, "55423-8", "Step count", "steps", it.count.toDouble(), it.startTime, it.endTime))
            } ?: emptyList()
        },
        ObservationType.VO2_MAX to { record ->
            (record as? Vo2MaxRecord)?.let {
                listOf(createVital(patientId, "60842-2", "VO2 max", "mL/kg/min", it.vo2MillilitersPerMinuteKilogram, it.time))
            } ?: emptyList()
        }
    )

    /**
     * Converts a Health Connect record into a list of FHIR Observations for a specific type.
     */
    override fun mapToObservation(patientId: String, type: ObservationType, record: Record): List<Observation> {
        this.patientId = patientId
        return try   {
            recordTypeToObservationTypeMapper[type]?.invoke(record) ?: emptyList()
        } catch (e: Exception) {
            Log.e("FHIR", "Failed record mapping: ${e.message}")
            emptyList()
        }
    }

    /**
     * Creates a FHIR Observation representing a vital sign.
     */
    private fun createVital(
        patientId: String,
        code: String,
        display: String,
        unit: String,
        value: Double,
        time: Instant
    ): Observation {
        return Observation().apply {
            status = Observation.ObservationStatus.FINAL
            category = listOf(CodeableConcept(Coding(CATEGORY_SYSTEM, "vital-signs", "Vital Signs")))
            this.code = CodeableConcept(Coding(LOINC_SYSTEM, code, display))
            subject = Reference("Patient/$patientId")
            effective = DateTimeType(formatter.format(time))
            setValue(
                Quantity().apply {
                    this.value = value.toBigDecimal()
                    this.unit = unit
                    this.system = UNIT_SYSTEM
                    this.code = unit
                }
            )
        }
    }

    /**
     * Creates a FHIR Observation representing an activity (e.g., step count, distance, sleep).
     */
    private fun createActivity(
        patientId: String,
        code: String,
        display: String,
        unit: String,
        value: Double,
        start: Instant,
        end: Instant
    ): Observation {
        return Observation().apply {
            status = Observation.ObservationStatus.FINAL
            category = listOf(CodeableConcept(Coding(CATEGORY_SYSTEM, "activity", "Activity")))
            this.code = CodeableConcept(Coding(LOINC_SYSTEM, code, display))
            subject = Reference("Patient/$patientId")
            effective = Period().apply {
                startElement = DateTimeType(formatter.format(start))
                endElement = DateTimeType(formatter.format(end))
            }
            setValue(
                Quantity().apply {
                    this.value = value.toBigDecimal()
                    this.unit = unit
                    this.system = UNIT_SYSTEM
                    this.code = unit
                }
            )
        }
    }



}
