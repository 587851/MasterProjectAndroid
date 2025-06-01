package com.example.masterprojectandroid.healthconnect.utils

import android.os.Build
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import com.example.masterprojectandroid.models.PermissionInfo

const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1
const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"


/**
 * A list of labeled permission entries used to:
 * - Display permission status to the user
 * - Manage and request permissions dynamically
 * Each permission is paired with a descriptive label via the PermissionInfo data class.
 */
val labeledPermissions = listOf(
    PermissionInfo("Basal Body Temperature (read)", HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class)),
    PermissionInfo("Basal Metabolic Rate (read)", HealthPermission.getReadPermission(BasalMetabolicRateRecord::class)),
    PermissionInfo("Body Fat (read)", HealthPermission.getReadPermission(BodyFatRecord::class)),
    PermissionInfo("Body Temperature (read)", HealthPermission.getReadPermission(BodyTemperatureRecord::class)),
    PermissionInfo("Body Water Mass (read)", HealthPermission.getReadPermission(BodyWaterMassRecord::class)),
    PermissionInfo("Distance (read)", HealthPermission.getReadPermission(DistanceRecord::class)),
    PermissionInfo("Heart Rate (read)", HealthPermission.getReadPermission(HeartRateRecord::class)),
    PermissionInfo("Heart Rate Variability (read)", HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)),
    PermissionInfo("Oxygen Saturation (read)", HealthPermission.getReadPermission(OxygenSaturationRecord::class)),
    PermissionInfo("Respiratory Rate (read)", HealthPermission.getReadPermission(RespiratoryRateRecord::class)),
    PermissionInfo("Resting Heart Rate (read)", HealthPermission.getReadPermission(RestingHeartRateRecord::class)),
    PermissionInfo("Sleep (read)", HealthPermission.getReadPermission(SleepSessionRecord::class)),
    PermissionInfo("Steps (read)", HealthPermission.getReadPermission(StepsRecord::class)),
    PermissionInfo("VO2 Max (read)", HealthPermission.getReadPermission(Vo2MaxRecord::class)),
    PermissionInfo("Background tasks", HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND),
    PermissionInfo("Read data older than 30 days", HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY))

/**
 * Extracts only the raw permission strings from labeledPermissions
 * to be used for permission requests or comparisons.
 */
val PERMISSIONS = labeledPermissions.map { it.permission }.toSet()

