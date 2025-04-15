package com.example.pawshield.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Diagnosis : Screen("diagnosis")
    object Result : Screen("result")
    object History : Screen("history")
    object HistoryDetail : Screen("history_detail")
    object About : Screen("about")
}