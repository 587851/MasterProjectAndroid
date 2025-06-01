package com.example.masterprojectandroid.healthconnect.services

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.example.masterprojectandroid.healthconnect.interfaces.IHealthConnectClientProvider

/**
 * Provides access to the Health Connect SDK status.
 */
class HealthConnectClientProvider : IHealthConnectClientProvider {
    override fun getSdkStatus(context: Context, packageName: String): Int {
        return HealthConnectClient.getSdkStatus(context, packageName)
    }
}