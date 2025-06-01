package com.example.masterprojectandroid.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "synced_records")
data class SyncedRecord(
    @PrimaryKey val recordId: String,
    val measuredAt: Long
)

