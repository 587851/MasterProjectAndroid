package com.example.masterprojectandroid.healthconnect.utils

import java.time.Instant

/**
 * Represents a single point of health data for use in charts and graphs.
 **/
data class HealthDataPoint(val timestamp: Instant, val value: Double) {}
