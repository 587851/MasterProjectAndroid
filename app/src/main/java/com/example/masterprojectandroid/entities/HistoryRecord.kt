package com.example.masterprojectandroid.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val dataType: String,
    val dataPointCount: Int,
    val periodStart: Long,
    val periodEnd: Long,
    val source: String
)


