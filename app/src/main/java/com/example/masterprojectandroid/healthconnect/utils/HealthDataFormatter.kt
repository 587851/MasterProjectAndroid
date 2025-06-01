package com.example.masterprojectandroid.healthconnect.utils

import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formats Health Connect data records into human-readable text or
 * structured data points for use in UI display and visualization.
 */
class HealthDataFormatter {

    /**
     * Converts a list of Health Connect records into a readable string format.
     */
    fun formatHealthDataToString(records: List<Record>): String {
        val recordData = StringBuilder()
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        var count = 0

        for (record in records) {
            when (record) {
                is BasalBodyTemperatureRecord -> {
                    recordData.append("Basal Body Temperature: %.1f°C\n".format(record.temperature.inCelsius))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is BasalMetabolicRateRecord -> {
                    recordData.append("Basal Metabolic Rate: %.0f kcal\n".format(record.basalMetabolicRate.inKilocaloriesPerDay))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is BodyFatRecord -> {
                    recordData.append("Body Fat: %.1f%%\n".format(record.percentage.value))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is BodyTemperatureRecord -> {
                    recordData.append("Body Temperature: %.1f°C\n".format(record.temperature.inCelsius))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is BodyWaterMassRecord -> {
                    recordData.append("Body Water Mass: %.1f kg\n".format(record.mass.inKilograms))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is DistanceRecord -> {
                    recordData.append("Distance: %.1f meters\n".format(record.distance.inMeters))
                    recordData.append("Start: ${formatter.format(record.startTime)}\n")
                    recordData.append("End: ${formatter.format(record.endTime)}\n\n")
                    count++
                }

                is HeartRateRecord -> {
                    record.samples.asReversed().forEach { sample ->
                        recordData.append("Heart rate: ${sample.beatsPerMinute}\n")
                        recordData.append("Time: ${formatter.format(sample.time)}\n\n")
                        count++
                    }
                }

                is HeartRateVariabilityRmssdRecord -> {
                    recordData.append("Heart Rate Variability: %.0f ms\n".format(record.heartRateVariabilityMillis))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is OxygenSaturationRecord -> {
                    recordData.append("Oxygen Saturation: %.1f%%\n".format(record.percentage))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is RespiratoryRateRecord -> {
                    recordData.append("Respiratory Rate: %.1f\n".format(record.rate))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is RestingHeartRateRecord -> {
                    recordData.append("Resting Heart Rate: ${record.beatsPerMinute}\n")
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }

                is SleepSessionRecord -> {
                    recordData.append(
                        "Sleep Session - Start: ${formatter.format(record.startTime)} - End: ${
                            formatter.format(
                                record.endTime
                            )
                        }\n"
                    )
                    record.stages.asReversed().forEach { sample ->
                        recordData.append("Sleep Stage: ${getStageDescription(sample.stage)}\n")
                        recordData.append("Start: ${formatter.format(sample.startTime)}\n")
                        recordData.append("End: ${formatter.format(sample.endTime)}\n")
                        count++
                    }
                    recordData.append("\n")
                }

                is StepsRecord -> {
                    recordData.append("Steps: ${record.count}\n")
                    recordData.append("Start: ${formatter.format(record.startTime)}\n")
                    recordData.append("End: ${formatter.format(record.endTime)}\n\n")
                    count++
                }

                is Vo2MaxRecord -> {
                    recordData.append("VO2 Max: %.1f mL/kg/min\n".format(record.vo2MillilitersPerMinuteKilogram))
                    recordData.append("Time: ${formatter.format(record.time)}\n\n")
                    count++
                }
            }
        }
        return "Total data points: $count\n\n$recordData"
    }

    /**
     * Maps Health Connect sleep stage codes to descriptive text for display.
     */
    fun getStageDescription(stageType: Int): String {
        return when (stageType) {
            1 -> "Awake, maybe in bed"
            7 -> "Awake, in bed."
            5 -> "Deep sleep stage."
            4 -> "Light sleep stage."
            3 -> "Assumed awake, out of bed"
            6 -> "REM sleep stage."
            2 -> "Asleep, but unknown sleep stage"
            0 -> "Stage of sleep is unknown."
            else -> "Unknown stage type"
        }
    }

    /**
     * Converts a list of Health Connect records into HealthDataPoint objects,
     * which can be used for visualizations.
     **/
    fun formatHealthDataToDataPoints(type: String, records: List<Record>): List<HealthDataPoint> {
        return when (type) {
            "Heart Rate" -> records.filterIsInstance<HeartRateRecord>().flatMap { record ->
                record.samples.map {
                    HealthDataPoint(it.time, it.beatsPerMinute.toDouble())
                }
            }

            "Steps" -> records.filterIsInstance<StepsRecord>().map {
                HealthDataPoint(it.startTime, it.count.toDouble())
            }

            "Body Fat" -> records.filterIsInstance<BodyFatRecord>().map {
                HealthDataPoint(it.time, it.percentage.value)
            }

            "Distance" -> records.filterIsInstance<DistanceRecord>().map {
                HealthDataPoint(it.startTime, it.distance.inKilometers)
            }

            "Oxygen Saturation" -> records.filterIsInstance<OxygenSaturationRecord>().map {
                HealthDataPoint(it.time, it.percentage.value)
            }

            "Basal Metabolic Rate" -> records.filterIsInstance<BasalMetabolicRateRecord>().map {
                HealthDataPoint(it.time, it.basalMetabolicRate.inKilocaloriesPerDay)
            }

            "Basal Body Temperature" -> records.filterIsInstance<BasalBodyTemperatureRecord>().map {
                HealthDataPoint(it.time, it.temperature.inCelsius)
            }

            "Body Temperature" -> records.filterIsInstance<BodyTemperatureRecord>().map {
                HealthDataPoint(it.time, it.temperature.inCelsius)
            }

            "Body Water Mass" -> records.filterIsInstance<BodyWaterMassRecord>().map {
                HealthDataPoint(it.time, it.mass.inKilograms)
            }

            "Heart Rate Variability" -> records.filterIsInstance<HeartRateVariabilityRmssdRecord>()
                .map {
                    HealthDataPoint(it.time, it.heartRateVariabilityMillis)
                }

            "Respiratory Rate" -> records.filterIsInstance<RespiratoryRateRecord>().map {
                HealthDataPoint(it.time, it.rate)
            }

            "Resting Heart Rate" -> records.filterIsInstance<RestingHeartRateRecord>().map {
                HealthDataPoint(it.time, it.beatsPerMinute.toDouble())
            }

            "VO2 Max" -> records.filterIsInstance<Vo2MaxRecord>().map {
                HealthDataPoint(it.time, it.vo2MillilitersPerMinuteKilogram)
            }

            "Sleep" -> records.filterIsInstance<SleepSessionRecord>().map {
                val durationHours =
                    java.time.Duration.between(it.startTime, it.endTime).toMinutes() / 60.0
                HealthDataPoint(it.startTime, durationHours)
            }

            else -> emptyList()
        }.sortedBy { it.timestamp }
    }


}
