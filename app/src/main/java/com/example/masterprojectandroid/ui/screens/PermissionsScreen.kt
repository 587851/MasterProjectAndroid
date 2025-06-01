package com.example.masterprojectandroid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.masterprojectandroid.viewmodels.PermissionsViewModel
import com.example.masterprojectandroid.viewmodels.PermissionsViewModel.UiEvent

/**
 * Displays the current Health Connect permissions status and allows the user to grant or refresh permissions.
 * - Shows a scrollable list of categorized permissions (read and other), with grant status.
 * - Includes actions to trigger permission requests and reload permission statuses.
 * - Displays contextual messages (e.g., Health Connect status or permission toast).
 */
@Composable
fun PermissionsScreen(
    permissionsViewModel: PermissionsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by permissionsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        permissionsViewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.ShowAllGrantedToast -> {
                    Toast.makeText(context, "All permissions already granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Permission Status", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        ) {
            val scrollState = rememberScrollState()
            val scrollProgress by remember {
                derivedStateOf {
                    if (scrollState.maxValue > 0)
                        scrollState.value.toFloat() / scrollState.maxValue
                    else 0f
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.healthConnectStatusMessage?.let {
                    Text("Health Connect Status", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (uiState.readPermissions.isNotEmpty()) {
                    Text("Read Permissions", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    uiState.readPermissions.forEach { (label, granted) ->
                        PermissionItem(label = label, granted = granted)
                    }
                }

                if (uiState.otherPermissions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Other Permissions", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    uiState.otherPermissions.forEach { (label, granted) ->
                        PermissionItem(label = label, granted = granted)
                    }
                }
            }

            if (scrollState.maxValue > 0) {
                LinearProgressIndicator(
                    progress = scrollProgress,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { permissionsViewModel.requestPermissions() },
            modifier = Modifier.width(250.dp)
        ) {
            Text("Give permissions")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { permissionsViewModel.refreshPermissions() },
            modifier = Modifier.width(250.dp)
        ) {
            Text("Reload Status")
        }
    }
}

@Composable
fun PermissionItem(label: String, granted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = if (granted) "✅" else "❌",
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}
