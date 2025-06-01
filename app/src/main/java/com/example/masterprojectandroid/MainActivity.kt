package com.example.masterprojectandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.masterprojectandroid.enums.Screen
import com.example.masterprojectandroid.ui.theme.MasterProjectAndroid
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterprojectandroid.ui.components.SharedTopAppBar
import com.example.masterprojectandroid.viewmodels.HistoryViewModel
import com.example.masterprojectandroid.viewmodels.MainViewModel
import com.example.masterprojectandroid.viewmodels.PatientViewModel
import com.example.masterprojectandroid.viewmodels.PermissionsViewModel
import com.example.masterprojectandroid.viewmodels.SettingsViewModel
import com.example.masterprojectandroid.viewmodels.StartupViewModel
import com.example.masterprojectandroid.ui.screens.*

/**
 * The main activity of the app which sets up the Composable content and handles navigation and ViewModel initialization.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deps = (application as MainApplication).dependencyProvider

        val permissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { result: Set<String> -> }

        deps.healthPermissionManager.registerPermissionLauncher(permissionLauncher)

        setContent {
            MasterProjectAndroid(dynamicColor = false) {
                val startupViewModel: StartupViewModel =
                    viewModel(factory = deps.startupViewModelFactory())
                LaunchedEffect(Unit) {
                    startupViewModel.onAppStart()
                }

                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Main.route
                val currentScreen =
                    Screen.entries.firstOrNull { it.route == currentRoute } ?: Screen.Main

                val mainViewModel: MainViewModel = viewModel(factory = deps.mainViewModelFactory())
                val historyViewModel: HistoryViewModel = viewModel()
                val settingsViewModel: SettingsViewModel =
                    viewModel(factory = deps.settingsViewModelFactory())
                val permissionsViewModel: PermissionsViewModel =
                    viewModel(factory = deps.permissionsViewModelFactory())
                val patientViewModel: PatientViewModel =
                    viewModel(factory = deps.patientViewModelFactory())

                permissionsViewModel.registerPermissionLauncher(permissionLauncher)

                Scaffold(
                    topBar = {
                        SharedTopAppBar(
                            currentScreen = currentScreen,
                            onScreenSelected = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Main.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(top = 16.dp)
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(mainViewModel, Modifier.fillMaxSize())
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(historyViewModel, Modifier.fillMaxSize())
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                settingsViewModel,
                                patientViewModel,
                                Modifier.fillMaxSize()
                            )
                        }
                        composable(Screen.Permissions.route) {
                            PermissionsScreen(permissionsViewModel, Modifier.fillMaxSize())
                        }
                    }

                }
            }
        }
    }
}



