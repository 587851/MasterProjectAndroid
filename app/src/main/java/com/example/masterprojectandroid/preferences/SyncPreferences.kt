package com.example.masterprojectandroid.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.masterprojectandroid.preferences.interfaces.ISyncPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.syncPreferencesDataStore by preferencesDataStore("sync_preferences")

/**
 * Manages synchronization-related preferences using Android's DataStore.
 */
class SyncPreferences(private val context: Context) : ISyncPreferences {

    companion object {
        // Preference keys used in DataStore
        val ALLOW_DUPLICATES = booleanPreferencesKey("allow_duplicates")
        val CLEANUP_AGE_DAYS = intPreferencesKey("cleanup_age_days")
        val AUTO_SYNC_FREQUENCY = intPreferencesKey("auto_sync_frequency")
        val AUTO_SYNC_TYPES = stringSetPreferencesKey("auto_sync_types")
    }

    /**
     * Flow that emits whether duplicate uploads are allowed.
     * Defaults to `false` if not set.
     */
    override val allowDuplicates: Flow<Boolean> =
        context.syncPreferencesDataStore.data.map { prefs ->
            prefs[ALLOW_DUPLICATES] ?: false
        }

    /**
     * Persists the user's preference for allowing duplicate records.
     */
    override suspend fun setAllowDuplicates(value: Boolean) {
        context.syncPreferencesDataStore.edit { prefs ->
            prefs[ALLOW_DUPLICATES] = value
        }
    }

    /**
     * Flow that emits the age threshold (in days) for record cleanup.
     * Defaults to `0` if not set.
     */
    override val cleanupAgeDays: Flow<Int> =
        context.syncPreferencesDataStore.data.map { prefs ->
            prefs[CLEANUP_AGE_DAYS] ?: 0
        }

    /**
     * Persists the number of days after which old records should be deleted.
     */
    override suspend fun setCleanupAgeDays(days: Int) {
        context.syncPreferencesDataStore.edit { prefs ->
            prefs[CLEANUP_AGE_DAYS] = days
        }
    }

    /**
     * Flow that emits the user's chosen auto-sync frequency (in minutes or hours).
     * Defaults to `0` if not set.
     */
    override val autoSyncFrequency: Flow<Int> =
        context.syncPreferencesDataStore.data.map { prefs ->
            prefs[AUTO_SYNC_FREQUENCY] ?: 0
        }

    /**
     * Persists the auto-sync frequency preference.
     */
    override suspend fun setAutoSyncFrequency(value: Int) {
        context.syncPreferencesDataStore.edit { prefs ->
            prefs[AUTO_SYNC_FREQUENCY] = value
        }
    }

    /**
     * Flow that emits a set of record types selected for automatic synchronization.
     */
    override val autoSyncTypes: Flow<Set<String>> =
        context.syncPreferencesDataStore.data.map { prefs ->
            prefs[AUTO_SYNC_TYPES] ?: emptySet()
        }

    /**
     * Persists the set of record types the user wants to sync automatically.
     */
    override suspend fun setAutoSyncTypes(types: Set<String>) {
        context.syncPreferencesDataStore.edit { prefs ->
            prefs[AUTO_SYNC_TYPES] = types
        }
    }
}
