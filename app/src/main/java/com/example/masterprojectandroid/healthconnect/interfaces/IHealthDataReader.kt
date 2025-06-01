package com.example.masterprojectandroid.healthconnect.interfaces

import androidx.health.connect.client.records.Record
import com.example.masterprojectandroid.healthconnect.utils.HealthDataPoint
import java.time.Instant

interface IHealthDataReader {
    suspend fun getIntervalRecords(type: String, start: Instant, end: Instant): List<Record>
    suspend fun convertRecordsToString(records: List<Record>): String
    suspend fun convertRecordsToPoints(type: String, records: List<Record>): List<HealthDataPoint>
}
