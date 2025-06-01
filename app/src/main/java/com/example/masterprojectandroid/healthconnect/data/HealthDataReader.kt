package com.example.masterprojectandroid.healthconnect.data

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthDataReader
import com.example.masterprojectandroid.healthconnect.utils.HealthDataFormatter
import com.example.masterprojectandroid.healthconnect.utils.HealthDataPoint
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Reads health data from Health Connect and converts it into different formats.
 */
class HealthDataReader(
    private val healthConnectClient: HealthConnectClient,
    private val healthDataFormatter: HealthDataFormatter
) : IHealthDataReader {

    /**
     * Maps string type names to actual Health Connect record classes.
     */
    private val recordTypeMap: Map<String, KClass<out Record>> = mapOf(
        "Basal Body Temperature" to BasalBodyTemperatureRecord::class,
        "Basal Metabolic Rate" to BasalMetabolicRateRecord::class,
        "Body Fat" to BodyFatRecord::class,
        "Body Temperature" to BodyTemperatureRecord::class,
        "Distance" to DistanceRecord::class,
        "Heart Rate" to HeartRateRecord::class,
        "Heart Rate Variability" to HeartRateVariabilityRmssdRecord::class,
        "Oxygen Saturation" to OxygenSaturationRecord::class,
        "Respiratory Rate" to RespiratoryRateRecord::class,
        "Resting Heart Rate" to RestingHeartRateRecord::class,
        "Sleep" to SleepSessionRecord::class,
        "Steps" to StepsRecord::class,
        "VO2 Max" to Vo2MaxRecord::class
    )


    /**
     * Reads all records of a given type within a time interval.
     * Uses paging to retrieve large datasets.
     */
    override suspend fun getIntervalRecords(type: String, start: Instant, end: Instant): List<Record> {
        val recordClass = recordTypeMap[type]
            ?: throw IllegalArgumentException("Invalid type: $type")
        val allRecords = mutableListOf<Record>()
        var pageToken: String? = null
        do {
            val request = ReadRecordsRequest(
                recordType = recordClass,
                timeRangeFilter = TimeRangeFilter.between(start, end),
                pageSize = 1000,
                pageToken = pageToken
            )
            val response = healthConnectClient.readRecords(request)
            allRecords += response.records
            pageToken = response.pageToken
            Log.d("HC", "Next pageToken: $pageToken")
        } while (!response.records.isEmpty() && pageToken.toString() != "")

        return allRecords
    }

    /**
     * Converts a list of Health Connect records into a human-readable string.
     */
    override suspend fun convertRecordsToString(records: List<Record>): String {
        return healthDataFormatter.formatHealthDataToString(records.reversed())
    }

    /**
     * Converts a list of Health Connect records into a structured list of HealthDataPoints
     * for use in charts and graphs.
     */
    override suspend fun convertRecordsToPoints(type: String, records: List<Record>): List<HealthDataPoint> {
        return healthDataFormatter.formatHealthDataToDataPoints(type, records)
    }
}
