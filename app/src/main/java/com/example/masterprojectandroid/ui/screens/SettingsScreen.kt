package com.example.masterprojectandroid.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.masterprojectandroid.viewmodels.PatientViewModel
import com.example.masterprojectandroid.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Displays user-configurable settings for syncing behavior and patient information.
 * - Patient Information: Allows editing and saving patient's name.
 * - Sync Settings:
 *   - Toggle to allow duplicate records.
 *   - Cleanup age setting for deleting old synced data.
 *   - Auto-sync frequency configuration.
 *   - Selection of which health data types should be auto-synced.
 **/
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    patientViewModel: PatientViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsViewModel.UiEvent.ResetFrequencyToNever -> {
                }
            }
        }
    }

    val cleanupOptions = listOf("Never", "1 day", "30 days", "90 days", "150 days", "365 days")
    val cleanupValues = listOf(0, 1, 30, 90, 150, 365)

    val frequencyOptions = listOf(
        "Never",
        "Every fifteen minute",
        "Every Hour",
        "Every Day",
        "Every Week",
        "Every Month"
    )
    val frequencyValues = listOf(0, 1, 2, 3, 4, 5)

    val groupedTypes = mapOf(
        "🫀 Vitals" to listOf(
            "Blood Pressure",
            "Heart Rate",
            "Heart Rate Variability",
            "Oxygen Saturation",
            "Resting Heart Rate",
            "Respiratory Rate",
        ),
        "🏃 Activity" to listOf(
            "Distance",
            "Steps",
            "VO2 Max"
        ),
        "🧍 Body" to listOf(
            "Basal Body Temperature",
            "Basal Metabolic Rate",
            "Body Fat",
            "Body Temperature",
        ),
    )

    var cleanupMenuExpanded by remember { mutableStateOf(false) }
    var freqMenuExpanded by remember { mutableStateOf(false) }
    var showTypeSelector by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PatientInfoSection(viewModel = patientViewModel)
            Divider(modifier = Modifier.fillMaxWidth())

            AllowDuplicatesSection(checked = uiState.allowDuplicates) {
                settingsViewModel.setAllowDuplicates(it)
            }

            Divider(modifier = Modifier.fillMaxWidth())

            CleanupAgeSection(
                selectedValue = uiState.cleanupAgeDays,
                options = cleanupOptions,
                values = cleanupValues,
                expanded = cleanupMenuExpanded,
                onExpand = { cleanupMenuExpanded = true },
                onDismiss = { cleanupMenuExpanded = false },
                onSelect = settingsViewModel::setCleanupAgeDays
            )

            Divider(modifier = Modifier.fillMaxWidth())

            AutoSyncFrequencySection(
                selectedValue = uiState.autoSyncFrequency,
                options = frequencyOptions,
                values = frequencyValues,
                expanded = freqMenuExpanded,
                onExpand = { freqMenuExpanded = true },
                onDismiss = { freqMenuExpanded = false },
                onSelect = settingsViewModel::setAutoSyncFrequency
            )

            OutlinedButton(
                onClick = {
                    showTypeSelector = !showTypeSelector
                    if (!showTypeSelector) return@OutlinedButton
                    coroutineScope.launch {
                        delay(500)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Auto-Sync Data Types")
            }

            TypeSelectorSection(
                visible = showTypeSelector,
                groupedTypes = groupedTypes,
                selectedTypes = uiState.autoSyncTypes,
                onTypeChange = settingsViewModel::setAutoSyncTypes,
                scrollProgress = scrollProgress
            )
        }
    }
}


@Composable
fun PatientInfoSection(viewModel: PatientViewModel) {
    val patientInfo by viewModel.patientInfo.collectAsState()

    var givenName by remember(patientInfo.givenName) { mutableStateOf(patientInfo.givenName) }
    var familyName by remember(patientInfo.familyName) { mutableStateOf(patientInfo.familyName) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Patient Information", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = givenName,
            onValueChange = { givenName = it },
            label = { Text("Given Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Family Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.updateName(givenName, familyName) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save Name")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Patient ID: ${patientInfo.id ?: "No ID"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AllowDuplicatesSection(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Allow duplicated data to be sent")
        }
    }
}

@Composable
fun CleanupAgeSection(
    selectedValue: Int,
    options: List<String>,
    values: List<Int>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Text("Auto-delete synced records older than:", textAlign = TextAlign.Center)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onExpand, modifier = Modifier.fillMaxWidth()) {
            Text(options[values.indexOf(selectedValue)], textAlign = TextAlign.Center)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            values.forEachIndexed { index, value ->
                DropdownMenuItem(
                    text = { Text(options[index], textAlign = TextAlign.Center) },
                    onClick = {
                        onSelect(value)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun AutoSyncFrequencySection(
    selectedValue: Int,
    options: List<String>,
    values: List<Int>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Text("Auto-Sync Frequency", textAlign = TextAlign.Center)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onExpand, modifier = Modifier.fillMaxWidth()) {
            Text(options[values.indexOf(selectedValue)], textAlign = TextAlign.Center)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            values.forEachIndexed { index, value ->
                DropdownMenuItem(
                    text = { Text(options[index], textAlign = TextAlign.Center) },
                    onClick = {
                        onSelect(value)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TypeSelectorSection(
    visible: Boolean,
    groupedTypes: Map<String, List<String>>,
    selectedTypes: Set<String>,
    onTypeChange: (Set<String>) -> Unit,
    scrollProgress: Float
) {
    AnimatedVisibility(visible = visible) {
        val groupExpansionStates = remember(visible) {
            mutableStateMapOf<String, Boolean>().apply {
                groupedTypes.keys.forEach { put(it, false) }
            }
        }

        val coroutineScope = rememberCoroutineScope()

        Column {
            LinearProgressIndicator(
                progress = scrollProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(vertical = 4.dp)
            )

            groupedTypes.entries.forEachIndexed { index, (category, types) ->
                val expanded = groupExpansionStates[category] ?: false
                val bringIntoViewRequester = remember { BringIntoViewRequester() }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .bringIntoViewRequester(bringIntoViewRequester),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    groupExpansionStates[category] = !expanded

                                    if (!expanded) {
                                        coroutineScope.launch {
                                            delay(200)
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column {
                                types.forEach { type ->
                                    val isChecked = selectedTypes.contains(type)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                val updated = if (isChecked)
                                                    selectedTypes - type
                                                else
                                                    selectedTypes + type
                                                onTypeChange(updated)
                                            }
                                        )
                                        Text(text = type)
                                    }
                                }
                            }
                        }
                    }
                }

                if (index < groupedTypes.size - 1) {
                    Divider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
