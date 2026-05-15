package com.example.builddaily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.builddaily.ui.components.FlashMessageOverlay
import com.example.builddaily.ui.history.HistoryScreen
import com.example.builddaily.ui.home.HomeScreen
import com.example.builddaily.ui.profile.MoreScreen
import com.example.builddaily.ui.profile.NotificationSettingsScreen
import com.example.builddaily.ui.profile.SettingsScreen
import com.example.builddaily.ui.splash.SplashScreen
import com.example.builddaily.ui.stats.StatsScreen
import com.example.builddaily.ui.pomodoro.PomodoroScreen
import com.example.builddaily.ui.pomodoro.PomodoroViewModel
import com.example.builddaily.ui.pomodoro.PomodoroHistoryScreen
import com.example.builddaily.ui.profile.LifeArchitectureScreen
import com.example.builddaily.ui.todo.TodoListScreen
import com.example.builddaily.ui.todo.AddTodoScreen
import com.example.builddaily.ui.hydration.HydrationScreen
import com.example.builddaily.ui.buylist.BuyListScreen
import com.example.builddaily.ui.booklibrary.BookLibraryScreen
import com.example.builddaily.data.repository.PomodoroRepository
import com.example.builddaily.data.repository.UserStatsRepository
import com.example.builddaily.data.repository.BuyListRepository
import com.example.builddaily.data.repository.BookRepository
import com.example.builddaily.ui.buylist.BuyListViewModel
import com.example.builddaily.ui.booklibrary.BookLibraryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.builddaily.data.security.SecurityRepository
import com.example.builddaily.ui.security.SecurityViewModel
import com.example.builddaily.ui.security.SecurityLockScreen
import com.example.builddaily.ui.security.SecuritySettingsScreen
import com.example.builddaily.ui.security.SecuritySetupScreen

@Composable
fun BuildDailyApp(securityRepository: SecurityRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val deviceId = DeviceIdManager.getDeviceId(context)
    val repository = remember(deviceId) { TaskRepository(context, deviceId) }
    val pomodoroRepo = remember { PomodoroRepository(context) }
    val statsRepository = remember { UserStatsRepository(context) }
    val buyListRepository = remember { BuyListRepository(context) }
    val bookRepository = remember { BookRepository(context) }

    // Initialize ViewModels
    val buyListViewModel = remember { BuyListViewModel(buyListRepository) }
    val bookLibraryViewModel = remember { BookLibraryViewModel(bookRepository) }
    val securityViewModel = remember { SecurityViewModel(securityRepository) }

    // Shared Pomodoro ViewModel to persist across navigation
    val pomodoroViewModel: PomodoroViewModel = remember { PomodoroViewModel(pomodoroRepo) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentRoute != "splash" && currentRoute != "security_lock" && currentRoute != "add_task" && currentRoute?.startsWith("edit_task") != true) {
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
                    if (initialState.destination.route == "splash" || initialState.destination.route == "security_lock") {
                        fadeIn(animationSpec = tween(400))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    if (initialState.destination.route == "splash" || initialState.destination.route == "security_lock") {
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
                        if (securityRepository.isAppLocked()) {
                            navController.navigate("security_lock") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    })
                }
                composable("security_lock") {
                    SecurityLockScreen(
                        viewModel = securityViewModel,
                        onAuthenticated = {
                            navController.navigate("home") {
                                popUpTo("security_lock") { inclusive = true }
                            }
                        }
                    )
                }
                composable("security_settings") {
                    SecuritySettingsScreen(
                        viewModel = securityViewModel,
                        repository = securityRepository,
                        onNavigateToSetup = { navController.navigate("security_setup") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("security_setup") {
                    SecuritySetupScreen(
                        viewModel = securityViewModel,
                        onComplete = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("home") {
                    HomeScreen(
                        repository = repository,
                        statsRepository = statsRepository,
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
                    StatsScreen(repository = repository, statsRepository = statsRepository)
                }
                composable("more") {
                    MoreScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToPomodoro = { navController.navigate("pomodoro") },
                        onNavigateToTodoList = { navController.navigate("to_do_list") },
                        onNavigateToEvolution = { navController.navigate("evolution") },
                        onNavigateToHydration = { navController.navigate("hydration") },
                        onNavigateToBuyList = { navController.navigate("buy_list") },
                        onNavigateToBookLibrary = { navController.navigate("book_library") }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateToSecurity = { navController.navigate("security_settings") },
                        onNavigateToNotifications = { navController.navigate("notification_settings") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("evolution") {
                    LifeArchitectureScreen(
                        statsRepository = statsRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("notification_settings") {
                    NotificationSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable("pomodoro") {
                    PomodoroScreen(
                        onBack = { navController.popBackStack() },
                        onShowHistory = { navController.navigate("pomodoro_history") },
                        viewModel = pomodoroViewModel
                    )
                }
                composable("pomodoro_history") {
                    PomodoroHistoryScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = pomodoroViewModel
                    )
                }
                composable("to_do_list") {
                    TodoListScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToAddTodo = { navController.navigate("add_todo") },
                        statsRepository = statsRepository
                    )
                }
                composable("add_todo") {
                    AddTodoScreen(
                        onBack = { navController.popBackStack() },
                        statsRepository = statsRepository
                    )
                }
                composable("hydration") {
                    HydrationScreen(
                        onBack = { navController.popBackStack() },
                        statsRepository = statsRepository
                    )
                }
                composable("buy_list") {
                    BuyListScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = buyListViewModel
                    )
                }
                composable("book_library") {
                    BookLibraryScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = bookLibraryViewModel
                    )
                }
            }
        }
        
        // Custom creative toast overlay
        FlashMessageOverlay()
    }
}
