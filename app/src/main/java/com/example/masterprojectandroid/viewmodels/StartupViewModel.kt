package com.example.masterprojectandroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.preferences.SyncPreferences
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class StartupViewModel(
    private val syncPreferences: SyncPreferences,
    private val syncedRecordRepository: ISyncedRecordRepository,
    application: Application
) : AndroidViewModel(application) {

    fun onAppStart() {
        viewModelScope.launch {
            cleanupOldRecords()
        }
    }

    private suspend fun cleanupOldRecords() {
        try {
            val days = syncPreferences.cleanupAgeDays.first()
            if (days > 0) {
                val threshold = Instant.now().minus(days.toLong(), ChronoUnit.DAYS).toEpochMilli()
                syncedRecordRepository.deleteOlderThan(threshold)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
