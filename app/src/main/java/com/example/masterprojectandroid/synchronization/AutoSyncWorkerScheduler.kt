package com.example.masterprojectandroid.synchronization

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.Duration

/**
 * Responsible for scheduling background synchronization tasks using WorkManager.
 */
class AutoSyncWorkerScheduler(val context: Context) {

    /**
     * Schedules a periodic background sync job using
     * WorkManager based on the given frequency.
     * If the frequency is not recognized, no job is scheduled.
     **/
    fun scheduleAutoSyncWorker(frequency: Int) {
        val interval = when (frequency) {
            1 -> Duration.ofMinutes(15)
            2 -> Duration.ofHours(1)
            3 -> Duration.ofDays(1)
            4 -> Duration.ofDays(7)
            5 -> Duration.ofDays(31)
            else -> {
                Log.i("AutoSyncWorker", "Auto-sync not scheduled. Frequency: $frequency")
                return
            }
        }
        Log.i("AutoSyncWorker", "Scheduling auto-sync every ${interval.toHours()} hours")
        val request = PeriodicWorkRequestBuilder<AutoSyncWorker>(interval)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AutoSyncWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Log.i("AutoSyncWorker", "Auto-sync worker added with interval: $interval")
    }
}