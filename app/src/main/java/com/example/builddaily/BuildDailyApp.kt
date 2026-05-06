package com.example.builddaily

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.builddaily.data.DeviceIdManager
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.ui.addtask.AddTaskScreen
import com.example.builddaily.ui.components.BottomNavBar
import com.example.builddaily.ui.history.HistoryScreen
import com.example.builddaily.ui.home.HomeScreen
import com.example.builddaily.ui.profile.ProfileScreen
import com.example.builddaily.ui.splash.SplashScreen
import com.example.builddaily.ui.stats.StatsScreen

@Composable
fun BuildDailyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val deviceId = DeviceIdManager.getDeviceId(context)
    val repository = remember(deviceId) { TaskRepository(context, deviceId) }

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash" && currentRoute != "add_task" && currentRoute?.startsWith("edit_task") != true) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                if (initialState.destination.route == "splash") {
                    fadeIn(animationSpec = tween(400))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                if (initialState.destination.route == "splash") {
                    fadeOut(animationSpec = tween(400))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 2 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable("splash") {
                SplashScreen(onAnimationFinished = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
            composable("home") {
                HomeScreen(
                    repository = repository,
                    onAddTask = { navController.navigate("add_task") },
                    onEditTask = { taskId -> navController.navigate("edit_task/$taskId") }
                )
            }
            composable("add_task") {
                AddTaskScreen(
                    repository = repository,
                    onTaskSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_task/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                AddTaskScreen(
                    repository = repository,
                    taskId = taskId,
                    onTaskSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(
                    repository = repository,
                    onEditTask = { taskId -> navController.navigate("edit_task/$taskId") }
                )
            }
            composable("stats") {
                StatsScreen(repository = repository)
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}
