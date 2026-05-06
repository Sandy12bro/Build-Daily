package com.example.builddaily.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.builddaily.ui.screens.CalendarScreen
import com.example.builddaily.ui.screens.DiaryScreen
import com.example.builddaily.ui.screens.HomeScreen
import com.example.builddaily.ui.screens.InsightsScreen
import com.example.builddaily.ui.screens.LoginScreen
import com.example.builddaily.ui.screens.TodayScreen

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Diary : Screen("diary")
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Insights : Screen("insights", "Insights", Icons.Default.Info)
    object Home : Screen("home", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Calendar,
    Screen.Insights,
    Screen.Home
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildDailyNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title!!) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(padding)
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
                    onNavigateToDiary = {
                        navController.navigate(Screen.Diary.route)
                    }
                )
            }
            composable(Screen.Diary.route) {
                DiaryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            composable(Screen.Home.route) {
                HomeScreen()
            }
        }
    }
}
