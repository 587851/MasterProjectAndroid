package com.example.masterprojectandroid.healthconnect.interfaces

import androidx.activity.result.ActivityResultLauncher

/**
 * Interface for managing Health Connect permissions, including checking,
 * requesting, and grouping permissions.
 */
interface IHealthPermissionManager {
    fun registerPermissionLauncher(launcher: ActivityResultLauncher<Set<String>>)
    suspend fun hasAllPermissions(): Boolean
    fun requestPermissions(permissions: Set<String>, onAllGranted: (() -> Unit)? = null)
    suspend fun getPermissionStatusGroups(): Pair<List<Pair<String, Boolean>>, List<Pair<String, Boolean>>>
    suspend fun hasPermissionFor(type: String): Boolean
    fun requestReadPermissionFor(type: String, onGranted: () -> Unit)
}
