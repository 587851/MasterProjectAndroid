package com.example.masterprojectandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.masterprojectandroid.healthconnect.utils.HealthDataPoint
import com.example.masterprojectandroid.ui.components.CustomDropdownSelector
import com.example.masterprojectandroid.viewmodels.MainViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.example.masterprojectandroid.ui.theme.HelseVestBlue
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Main UI screen for interacting with health data.
 * Users can toggle display modes (Text, Bar, Graph), view data,
 * and trigger read/send operations for selected data types and time ranges.
 */
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val displayText = uiState.displayText
    val isLoading = uiState.isLoading
    val displayMode by mainViewModel.displayMode.collectAsState()
    val chartData by mainViewModel.chartData.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .then(if (isLoading) Modifier.alpha(0.3f) else Modifier),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { mainViewModel.updateDisplayMode(MainViewModel.DisplayMode.TEXT) }) {
                    Text("Text")
                }
                Button(onClick = { mainViewModel.updateDisplayMode(MainViewModel.DisplayMode.BAR) }) {
                    Text("Bar")
                }
                Button(onClick = { mainViewModel.updateDisplayMode(MainViewModel.DisplayMode.GRAPH) }) {
                    Text("Graph")
                }
            }

            var chartHeightPx by remember { mutableStateOf(0) }
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .onSizeChanged { chartHeightPx = it.height }
            ) {
                val chartHeightDp = with(density) { chartHeightPx.toDp() }

                Column(modifier = Modifier.fillMaxSize()) {
                    val scrollState = rememberScrollState()
                    val scrollProgress by remember {
                        derivedStateOf {
                            if (scrollState.maxValue > 0)
                                scrollState.value.toFloat() / scrollState.maxValue
                            else 0f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        when (displayMode) {
                            MainViewModel.DisplayMode.TEXT -> Text(text = displayText)
                            MainViewModel.DisplayMode.BAR -> SimpleBarChart(
                                chartData,
                                modifier = Modifier.height(chartHeightDp - 30.dp)
                            )
                            MainViewModel.DisplayMode.GRAPH -> SimpleLineChart(
                                chartData,
                                modifier = Modifier.height(chartHeightDp - 30.dp)
                            )
                        }

                    }

                    if (scrollState.maxValue > 0) {
                        LinearProgressIndicator(
                            progress = scrollProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomDropdownSelector(
                label = "Data Type",
                options = mainViewModel.types,
                selectedOption = uiState.selectedType,
                onSelection = { mainViewModel.updateSelectedType(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomDropdownSelector(
                label = "Time Range",
                options = mainViewModel.timeOptions,
                selectedOption = uiState.selectedTime,
                onSelection = { mainViewModel.updateSelectedTime(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { mainViewModel.readHealthDataForSelectedPeriod() },
                modifier = Modifier.width(250.dp)
            ) {
                Text("Read Data")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { mainViewModel.sendHealthDataForSelectedPeriod() },
                modifier = Modifier.width(250.dp)
            ) {
                Text("Send Data to DB")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sending data to server...")
                }
            }
        }
    }
}

/**
 * Renders a horizontally scrollable bar chart of health data points.
 * Each bar represents a data point with optional highlighting on tap.
 **/
@Composable
fun SimpleBarChart(
    data: List<HealthDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = HelseVestBlue,
    gridColor: Color = Color.LightGray,
    selectedBarColor: Color = Color(0xFF3F51B5)
) {
    if (data.isEmpty()) return
    val chartData = data.take(100) //only show the first 100

    val maxValue = (chartData.maxOfOrNull { it.value } ?: 1.0)
    val minValue = 0.0
    val valueRange = maxValue - minValue

    val scrollState = rememberScrollState()
    val touchPoint = remember { mutableStateOf<Offset?>(null) }
    val selectedIndex = remember { mutableStateOf<Int?>(null) }

    val barWidthDp = 16.dp
    val barSpacingDp = 16.dp
    val leftPaddingDp = 56.dp
    val totalWidth = leftPaddingDp + (barWidthDp + barSpacingDp) * chartData.size

    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm").withZone(ZoneId.systemDefault())

    Column(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        Box {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPoint.value = offset
                        }
                    }
            ) {
                val barWidthPx = barWidthDp.toPx()
                val spacingPx = barSpacingDp.toPx()
                val leftPaddingPx = leftPaddingDp.toPx()
                val canvasHeight = size.height
                val canvasWidth = size.width

                val yRatio = if (valueRange != 0.0) (canvasHeight / valueRange).toFloat() else 0f

                val labelTextPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                }

                val steps = 5
                for (i in 0..steps) {
                    val y = (canvasHeight / steps) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format("%.1f", maxValue - (valueRange * i / steps)),
                        0f,
                        y - 5f,
                        labelTextPaint
                    )
                }

                chartData.forEachIndexed { index, point ->
                    val barHeight = ((point.value - minValue).toFloat() * yRatio)
                    val x = index * (barWidthPx + spacingPx) + leftPaddingPx
                    val y = canvasHeight - barHeight
                    val isSelected = selectedIndex.value == index

                    drawRect(
                        color = if (isSelected) selectedBarColor else barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidthPx, barHeight)
                    )

                    if (isSelected) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "${point.value}",
                            x + barWidthPx / 4,
                            y - 12,
                            labelTextPaint
                        )
                    }

                    touchPoint.value?.let { touch ->
                        if (touch.x in x..(x + barWidthPx)) {
                            selectedIndex.value = index
                        }
                    }
                }

                chartData.forEachIndexed { index, point ->
                    val x = index * (barWidthPx + spacingPx) + leftPaddingPx
                    val label = dateFormatter.format(point.timestamp)
                    val lines = label.split(" ")

                    lines.forEachIndexed { i, line ->
                        drawContext.canvas.nativeCanvas.drawText(
                            line,
                            x,
                            canvasHeight + 30f + i * 28f,
                            labelTextPaint
                        )
                    }
                }

            }
        }
    }
}

/**
 * Renders a horizontally scrollable line chart connecting health data points.
 * Users can tap to highlight and inspect values.
 **/
@Composable
fun SimpleLineChart(
    data: List<HealthDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = HelseVestBlue,
    pointColor: Color = Color.Red,
    gridColor: Color = Color.LightGray
) {
    if (data.isEmpty()) return
    val chartData = data.take(100) //only show the first 100
    val maxValue = (chartData.maxOfOrNull { it.value } ?: 1.0)
    val minValue = (chartData.minOfOrNull { it.value } ?: 0.0)
    val scrollState = rememberScrollState()
    val spacingDp = 32.dp
    val leftPaddingDp = 56.dp
    val chartWidth = leftPaddingDp + spacingDp * (chartData.size - 1).coerceAtLeast(1)

    val touchPoint = remember { mutableStateOf<Offset?>(null) }
    val selectedPoint = remember { mutableStateOf<HealthDataPoint?>(null) }

    val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm").withZone(ZoneId.systemDefault())

    Column(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        Box {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(chartWidth + 10.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPoint.value = offset
                        }
                    }
            ) {
                val spacingPx = spacingDp.toPx()
                val leftPaddingPx = leftPaddingDp.toPx()
                val canvasHeight = size.height
                val canvasWidth = size.width

                val valueRange = maxValue - minValue
                val yRatio = if (valueRange != 0.0) (canvasHeight / valueRange).toFloat() else 0f

                val points = chartData.mapIndexed { index, point ->
                    val x = index * spacingPx + leftPaddingPx
                    val y = canvasHeight - ((point.value - minValue) * yRatio).toFloat()
                    Offset(x, y)
                }

                val steps = 5
                val labelTextPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                }

                for (i in 0..steps) {
                    val y = (canvasHeight / steps) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        String.format("%.1f", maxValue - (valueRange * i / steps)),
                        0f,
                        y - 5f,
                        labelTextPaint
                    )
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                points.forEach {
                    drawCircle(
                        color = pointColor,
                        center = it,
                        radius = 6f
                    )
                }

                touchPoint.value?.let { touch ->
                    val closest = points.minByOrNull { abs(it.x - touch.x) }
                    val index = points.indexOf(closest)
                    if (index in chartData.indices) {
                        selectedPoint.value = chartData[index]
                        val selected = points[index]
                        drawCircle(color = Color.Yellow, center = selected, radius = 10f)
                        drawContext.canvas.nativeCanvas.drawText(
                            "${chartData[index].value}",
                            selected.x + 8,
                            selected.y - 16,
                            labelTextPaint
                        )
                    }
                }

                points.forEachIndexed { index, point ->
                    val label = formatter.format(chartData[index].timestamp)
                    val lines = label.split(" ")

                    lines.forEachIndexed { i, line ->
                        drawContext.canvas.nativeCanvas.drawText(
                            line,
                            point.x - 30f,
                            canvasHeight + 30f + i * 28f,
                            labelTextPaint
                        )
                    }
                }

            }
        }
    }
}




