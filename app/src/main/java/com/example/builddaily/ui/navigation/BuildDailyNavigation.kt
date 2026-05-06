package com.example.builddaily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.builddaily.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
}

@Composable
fun BuildDailyNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}
