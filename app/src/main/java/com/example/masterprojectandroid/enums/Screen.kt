package com.example.masterprojectandroid.enums

/**
 * Enum class representing different screens (routes) in the app's navigation.
 */
enum class Screen(val route: String, val label: String) {
    Main("main", "Main"),
    History("history", "History"),
    Settings("settings", "Settings"),
    Permissions("permissions", "Permissions")
}

