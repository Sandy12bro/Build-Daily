package com.example.builddaily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.builddaily.ui.screens.HomeScreen
import com.example.builddaily.ui.screens.LoginScreen
import com.example.builddaily.ui.screens.TodayScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Today : Screen("today")
    object PlanDay : Screen("plan_day")
    object Diary : Screen("diary")
    object Calendar : Screen("calendar")
    object Insights : Screen("insights")
    object Home : Screen("home")
}

@Composable
fun BuildDailyNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Today.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Login.route)
                    }
                }
            )
        }
        composable(Screen.Today.route) {
            TodayScreen(
                onNavigateToPlan = {
                    navController.navigate(Screen.PlanDay.route)
                }
            )
        }
        composable(Screen.PlanDay.route) {
            // TODO: Implement PlanDayScreen
        }
        composable(Screen.Diary.route) {
            // TODO: Implement DiaryScreen
        }
        composable(Screen.Calendar.route) {
            // TODO: Implement CalendarScreen
        }
        composable(Screen.Insights.route) {
            // TODO: Implement InsightsScreen
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}
