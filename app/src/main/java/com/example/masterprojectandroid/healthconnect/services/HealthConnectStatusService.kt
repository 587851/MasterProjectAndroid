package com.example.masterprojectandroid.healthconnect.services

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import com.example.masterprojectandroid.enums.HealthConnectAvailability
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthConnectStatusService
import com.example.masterprojectandroid.healthconnect.utils.HEALTH_CONNECT_PACKAGE
import com.example.masterprojectandroid.healthconnect.utils.MIN_SUPPORTED_SDK


/**
 * Provides information about the availability and support status of Health Connect on the device.
 **/
class HealthConnectStatusService(
    private val context: Context,
    private val clientProvider: HealthConnectClientProvider
):IHealthConnectStatusService {

    /**
     * Checks if the device's OS version meets the minimum SDK requirement for Health Connect.
     **/
    override fun isSupported(): Boolean = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    /**
     * Returns the current availability status of Health Connect on the device.
     **/
    override fun getAvailability(): HealthConnectAvailability {
        if (!isSupported()) return HealthConnectAvailability.NOT_SUPPORTED

        return when (clientProvider.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.NOT_SUPPORTED
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_INSTALLED
        }
    }
}
