package com.example.masterprojectandroid.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterprojectandroid.entities.HistoryRecord
import com.example.masterprojectandroid.preferences.SyncPreferences
import com.example.masterprojectandroid.enums.ObservationType
import com.example.masterprojectandroid.fhir.interfaces.IObservationUploader
import com.example.masterprojectandroid.healthconnect.data.HealthDataReader
import com.example.masterprojectandroid.healthconnect.services.HealthPermissionManager
import com.example.masterprojectandroid.healthconnect.utils.HealthDataPoint
import com.example.masterprojectandroid.repositories.interfaces.IHistoryRecordRepository
import com.example.masterprojectandroid.repositories.interfaces.ISyncedRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**

 * ViewModel that manages the logic for reading, displaying, and sending health data.
 * Responsibilities:
 * - Reads health data for a selected type and time range using HealthDataReader.
 * - Displays the data as text, bar chart, or line graph.
 * - Sends observations to a FHIR server, avoiding duplicates if specified.
 * - Maintains UI state via `MainUiState` and chart data via a StateFlow.
 * - Handles permission checks through HealthPermissionManager.
 * - Logs performance metrics for key operations.
 * - Records sync operations in the history repository.
 * Used by: MainScreen
 */
class MainViewModel(
    private val healthPermissionManager: HealthPermissionManager,
    private val healthDataReader: HealthDataReader,
    private val observationUploader: IObservationUploader,
    private val historyRecordRepository: IHistoryRecordRepository,
    private val syncedRecordRepository: ISyncedRecordRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _chartData = MutableStateFlow<List<HealthDataPoint>>(emptyList())
    val chartData: StateFlow<List<HealthDataPoint>> = _chartData

    enum class DisplayMode { TEXT, BAR, GRAPH }

    private val _displayMode = MutableStateFlow(DisplayMode.TEXT)
    val displayMode: StateFlow<DisplayMode> = _displayMode

    val syncPreferences = SyncPreferences(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    val types = listOf(
        "Basal Body Temperature",
        "Basal Metabolic Rate",
        "Body Fat",
        "Body Temperature",
        "Distance",
        "Heart Rate",
        "Heart Rate Variability",
        "Oxygen Saturation",
        "Respiratory Rate",
        "Resting Heart Rate",
        "Sleep",
        "Steps",
        "VO2 Max"
    )

    val timeOptions = listOf("Last week", "Last 24 hours", "Last month")

    private val observationTypeMap = mapOf(
        "Basal Body Temperature" to ObservationType.BASAL_BODY_TEMPERATURE,
        "Basal Metabolic Rate" to ObservationType.BASAL_METABOLIC_RATE,
        "Body Fat" to ObservationType.BODY_FAT,
        "Body Temperature" to ObservationType.BODY_TEMPERATURE,
        "Distance" to ObservationType.DISTANCE,
        "Heart Rate" to ObservationType.HEART_RATE,
        "Heart Rate Variability" to ObservationType.HEART_RATE_VARIABILITY,
        "Oxygen Saturation" to ObservationType.OXYGEN_SATURATION,
        "Respiratory Rate" to ObservationType.RESPIRATORY_RATE,
        "Resting Heart Rate" to ObservationType.RESTING_HEART_RATE,
        "Sleep" to ObservationType.SLEEP,
        "Steps" to ObservationType.STEPS,
        "VO2 Max" to ObservationType.VO2_MAX
    )

    /**
     * Reads health data for the selected type and time range.
     * Updates chart data and display text based on the retrieved records.
     */
    fun readHealthDataForSelectedPeriod() {
        withPermissionForSelectedType(action = {
            val (start, end) = getStartEndFromOption(_uiState.value.selectedTime)

            viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                val records = healthDataReader.getIntervalRecords(
                    _uiState.value.selectedType,
                    start,
                    end
                )
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                Log.d("Performance", "Data extraction took $duration ms")

                val parsedData =
                    healthDataReader.convertRecordsToPoints(_uiState.value.selectedType, records)
                _chartData.value = parsedData

                val startStr = dateToString(start)
                val endStr = dateToString(end)
                val header = "${_uiState.value.selectedType} data from $startStr to $endStr\n\n"

                val result = healthDataReader.convertRecordsToString(records)
                val text = if (result.isBlank()) {
                    "${_uiState.value.selectedType}: No data found from $startStr to $endStr."
                } else {
                    header + result
                }

                _uiState.value = _uiState.value.copy(displayText = text)
            }
        })
    }

    /**
     * Sends health data for the selected type and time range to the server.
     * Records sync history and displays a result message.
     */
    fun sendHealthDataForSelectedPeriod() {
        withPermissionForSelectedType(action = {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val startTime = System.currentTimeMillis()

                val selectedType = _uiState.value.selectedType
                val selectedTime = _uiState.value.selectedTime

                val obsType = observationTypeMap[selectedType]
                    ?: throw IllegalArgumentException("Unsupported type")

                val (start, end) = getStartEndFromOption(selectedTime)
                val records = healthDataReader.getIntervalRecords(selectedType, start, end)
                val allowDuplicates = syncPreferences.allowDuplicates.first()

                val sentCount = observationUploader.sendObservationsFromRecords(
                    type = obsType,
                    records = records,
                    syncedRecordRepository = syncedRecordRepository,
                    allowDuplicates = allowDuplicates
                )

                val message = "Data sent to server successfully \n" +
                        "Type: $selectedType \n" +
                        "Records sent: $sentCount \n" +
                        "Period: ${dateToString(start)} to ${dateToString(end)}\n"

                _uiState.value = _uiState.value.copy(displayText = message)

                if (sentCount > 0) {
                    historyRecordRepository.insertHistoryRecord(
                        HistoryRecord(
                            timestamp = Instant.now().toEpochMilli(),
                            dataType = selectedType,
                            dataPointCount = sentCount,
                            periodStart = start.toEpochMilli(),
                            periodEnd = end.toEpochMilli(),
                            source = "Manual"
                        )
                    )
                }
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                Log.d("Performance", "Data operation took $duration ms")

            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(displayText = "Failed to send data: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        })
    }

    /**
     * Ensures the app has permission for the selected health data type before running the action.
     * If not granted, requests permission and re-executes on success.
     */
    private fun withPermissionForSelectedType(
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            val selectedType = _uiState.value.selectedType
            val hasPermission = healthPermissionManager.hasPermissionFor(selectedType)

            if (!hasPermission) {
                healthPermissionManager.requestReadPermissionFor(selectedType) {
                    withPermissionForSelectedType(action)
                }
            } else {
                action()
            }
        }
    }

    /**
     * Converts the selected time range option into a start and end Instant.
     */
    private fun getStartEndFromOption(selected: String): Pair<Instant, Instant> {
        val end = Instant.now()
        val start = when (selected) {
            "Last 24 hours" -> end.minus(1, ChronoUnit.DAYS)
            "Last month" -> end.minus(30, ChronoUnit.DAYS)
            else -> end.minus(7, ChronoUnit.DAYS)
        }
        return start to end
    }

    /**
     * Formats an Instant into a readable date string (yyyy-MM-dd).
     */
    private fun dateToString(time: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
        return formatter.format(time)
    }

    /**
     * Updates the current display mode (TEXT, BAR, GRAPH).
     */
    fun updateDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
    }

    /**
     * Updates the selected health data type.
     */
    fun updateSelectedType(type: String) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    /**
     * Updates the selected time range for data queries.
     */
    fun updateSelectedTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }
}

data class MainUiState(
    val selectedType: String = "Heart Rate",
    val selectedTime: String = "Last week",
    val displayText: String = "",
    val isLoading: Boolean = false
)
