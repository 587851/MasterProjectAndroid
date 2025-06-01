package com.example.masterprojectandroid.ui.components


import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.masterprojectandroid.enums.Screen

/**
 * A shared top app bar composable that displays the current screen title
 * and allows switching screens via a dropdown menu.
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopAppBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentScreen.label,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = { Box(modifier = Modifier.size(48.dp)) },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Screen",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Screen.values().forEach { screen ->
                    DropdownMenuItem(
                        text = { Text(screen.label) },
                        onClick = {
                            expanded = false
                            onScreenSelected(screen)
                        }
                    )
                }
            }
        }
    )
}

/**
 * A customizable dropdown selector component.
 **/
@Composable
fun CustomDropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelection: (String) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 250.dp
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "ArrowRotation"
    )

    val maxVisibleItems = 4
    val itemHeight = 48.dp
    val maxDropdownHeight = itemHeight * maxVisibleItems

    val scrollState = rememberScrollState()

    Column(modifier = modifier.width(width), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$label: $selectedOption")
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = maxDropdownHeight)
                        .verticalScroll(scrollState)
                        .padding(vertical = 4.dp)
                ) {
                    options.forEach { option ->
                        TextButton(
                            onClick = {
                                onSelection(option)
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = option,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
