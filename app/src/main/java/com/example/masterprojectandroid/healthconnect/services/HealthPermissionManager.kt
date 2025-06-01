package com.example.masterprojectandroid.healthconnect.services

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthPermissionManager
import com.example.masterprojectandroid.healthconnect.utils.PERMISSIONS
import com.example.masterprojectandroid.healthconnect.utils.labeledPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages Health Connect permissions, including checking, requesting, and organizing permissions by type.
 * Handles both bulk and per-record-type permission workflows.
 */
class HealthPermissionManager(
    private val healthConnectClient: HealthConnectClient
) : IHealthPermissionManager {
    private var permissionLauncher: ActivityResultLauncher<Set<String>>? = null

    /**
     * Maps readable health data types to their corresponding read permissions.
     */
    private val permissionMap: Map<String, String> = mapOf(
        "Basal Body Temperature" to HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
        "Basal Metabolic Rate" to HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        "Body Fat" to HealthPermission.getReadPermission(BodyFatRecord::class),
        "Body Temperature" to HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        "Distance" to HealthPermission.getReadPermission(DistanceRecord::class),
        "Heart Rate" to HealthPermission.getReadPermission(HeartRateRecord::class),
        "Heart Rate Variability" to HealthPermission.getReadPermission(
            HeartRateVariabilityRmssdRecord::class
        ),
        "Oxygen Saturation" to HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        "Respiratory Rate" to HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        "Resting Heart Rate" to HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        "Sleep" to HealthPermission.getReadPermission(SleepSessionRecord::class),
        "Steps" to HealthPermission.getReadPermission(StepsRecord::class),
        "VO2 Max" to HealthPermission.getReadPermission(Vo2MaxRecord::class)
    )

    /**
     * Registers an ActivityResultLauncher to be used when launching the Health Connect permission UI.
     */
    override fun registerPermissionLauncher(launcher: ActivityResultLauncher<Set<String>>) {
        permissionLauncher = launcher
    }

    /**
     * Checks whether all required permissions in the PERMISSIONS set have been granted.
     */
    override suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient.permissionController
            .getGrantedPermissions()
            .containsAll(PERMISSIONS)
    }

    /**
     * Requests a set of permissions via the registered launcher.
     * If all permissions are already granted, invokes the optional callback immediately.
     */
    override fun requestPermissions(permissions: Set<String>, onAllGranted: (() -> Unit)?) {
        CoroutineScope(Dispatchers.Main).launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                onAllGranted?.invoke()
            } else {
                permissionLauncher?.launch(permissions)
                    ?: Log.e("HealthPermissionManager", "Permission launcher not registered")
            }
        }
    }

    /**
     * Groups permissions into two lists: read-related and non-read-related,
     * and pairs each permission label with its grant status.
     */
    override suspend fun getPermissionStatusGroups(): Pair<List<Pair<String, Boolean>>, List<Pair<String, Boolean>>> {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()

        val readPermissions = labeledPermissions
            .filter { it.label.contains("(read)", ignoreCase = true) }
            .map { it.label to granted.contains(it.permission) }

        val notReadPermissions = labeledPermissions
            .filter { !it.label.contains("(read)", ignoreCase = true) }
            .map { it.label to granted.contains(it.permission) }

        return readPermissions to notReadPermissions
    }

    /**
     * Checks whether the app has the required permission for a specific record type.
     */
    override suspend fun hasPermissionFor(type: String): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        val neededPermission = permissionMap[type] ?: return false
        return granted.contains(neededPermission)
    }

    /**
     * Requests permission for a specific record type by name.
     * If the permission is granted, executes the provided callback.
     */
    override fun requestReadPermissionFor(type: String, onGranted: () -> Unit) {
        val permission = permissionMap[type] ?: return
        requestPermissions(setOf(permission), onGranted)
    }
}
