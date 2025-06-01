package com.example.masterprojectandroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.appdatabase.AppDatabase
import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.repositories.implementations.HistoryRecordRepository
import com.example.masterprojectandroid.repositories.interfaces.IHistoryRecordRepository
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.Instant

/**
 * ViewModel responsible for retrieving and organizing history records.
 * Responsibilities:
 * - Fetches all history records from the repository as a Flow.
 * - Exposes `historyRecords` as a StateFlow of all records.
 * - Groups and categorizes records by recency into:
 *   - Last 24 Hours
 *   - Last Week
 *   - Older
 * - Maintains sorted ordering within each group for UI display.
 * Used by: HistoryScreen
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository: IHistoryRecordRepository = HistoryRecordRepository(db.historyRecordDao())

    val historyRecords: StateFlow<List<HistoryRecord>> =
        repository.getAllHistoryRecordsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedHistoryRecords: StateFlow<Map<String, List<HistoryRecord>>> =
        historyRecords
            .map { records ->
                val now = Instant.now()
                val grouped = records.groupBy { record ->
                    val duration = Duration.between(Instant.ofEpochMilli(record.timestamp), now)
                    when {
                        duration <= Duration.ofDays(1) -> "Last 24 Hours"
                        duration <= Duration.ofDays(7) -> "Last Week"
                        else -> "Older"
                    }
                }

                val desiredOrder = listOf("Last 24 Hours", "Last Week", "Older")
                val sorted = linkedMapOf<String, List<HistoryRecord>>()
                desiredOrder.forEach { key ->
                    grouped[key]?.sortedByDescending { it.timestamp }?.let { sorted[key] = it }
                }

                sorted
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


}

