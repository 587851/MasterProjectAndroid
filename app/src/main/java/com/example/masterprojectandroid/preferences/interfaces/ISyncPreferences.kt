package com.example.masterprojectandroid.preferences.interfaces

import kotlinx.coroutines.flow.Flow

interface ISyncPreferences {
    val allowDuplicates: Flow<Boolean>
    suspend fun setAllowDuplicates(value: Boolean)

    val cleanupAgeDays: Flow<Int>
    suspend fun setCleanupAgeDays(days: Int)

    val autoSyncFrequency: Flow<Int>
    suspend fun setAutoSyncFrequency(value: Int)

    val autoSyncTypes: Flow<Set<String>>
    suspend fun setAutoSyncTypes(types: Set<String>)
}
