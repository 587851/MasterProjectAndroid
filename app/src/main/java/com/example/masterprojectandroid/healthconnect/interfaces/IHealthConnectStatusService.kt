package com.example.masterprojectandroid.healthconnect.interfaces

import com.example.masterprojectandroid.enums.HealthConnectAvailability

interface IHealthConnectStatusService {
    fun isSupported(): Boolean
    fun getAvailability(): HealthConnectAvailability
}
