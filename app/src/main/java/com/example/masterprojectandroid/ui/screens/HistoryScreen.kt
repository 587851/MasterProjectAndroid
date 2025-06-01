package com.example.masterprojectandroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.viewmodels.HistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Composable screen displaying a grouped list of history records.
 * If no data is available, a placeholder message is shown.
 **/
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val groupedRecords by viewModel.groupedHistoryRecords.collectAsState()

    if (groupedRecords.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No history data available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = modifier.padding(vertical = 8.dp)) {
            groupedRecords.forEach { (timeRange, records) ->
                item {
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(records) { record ->
                    HistoryRecordItem(record)
                }
            }
        }
    }
}

/**
 * Displays a single history record in a card format, showing details
 * such as the data type, number of points, time period, and sync source.
 **/
@Composable
fun HistoryRecordItem(record: HistoryRecord) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${record.dataType} sent on ${formatter.format(Instant.ofEpochMilli(record.timestamp))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Data points: ${record.dataPointCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Period: ${formatter.format(Instant.ofEpochMilli(record.periodStart))} - ${
                    formatter.format(Instant.ofEpochMilli(record.periodEnd))
                }",
                style = MaterialTheme.typography.bodySmall
            )
            Text("Source: ${record.source}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
