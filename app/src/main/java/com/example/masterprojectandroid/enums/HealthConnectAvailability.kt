package com.example.masterprojectandroid.enums

/**
 * Enum representing the overall availability of Health Connect on a device.
 * Used by the app to determine whether Health Connect functionality should be enabled.
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}