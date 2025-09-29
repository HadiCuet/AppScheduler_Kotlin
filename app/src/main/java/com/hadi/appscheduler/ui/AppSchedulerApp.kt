package com.hadi.appscheduler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hadi.appscheduler.R
import com.hadi.appscheduler.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Schedules : Screen("schedules", "Schedules", Icons.Default.Schedule)
    object Logs : Screen("logs", "Logs", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSchedulerApp(
    repository: ScheduleRepository,
    appListProvider: AppListProvider,
    onRequestExactAlarmPermission: () -> Unit,
    onRequestUsageStatsPermission: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                listOf(Screen.Schedules, Screen.Logs).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedules.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Schedules.route) {
                ScheduleListScreen(
                    repository = repository,
                    appListProvider = appListProvider,
                    onRequestExactAlarmPermission = onRequestExactAlarmPermission,
                    navController = navController
                )
            }
            composable(Screen.Logs.route) {
                ExecutionLogsScreen(
                    repository = repository
                )
            }
            composable("create_schedule") {
                CreateEditScheduleScreen(
                    repository = repository,
                    appListProvider = appListProvider,
                    onRequestExactAlarmPermission = onRequestExactAlarmPermission,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_schedule/{scheduleId}") { backStackEntry ->
                val scheduleId = backStackEntry.arguments?.getString("scheduleId")?.toLongOrNull() ?: return@composable
                CreateEditScheduleScreen(
                    repository = repository,
                    appListProvider = appListProvider,
                    onRequestExactAlarmPermission = onRequestExactAlarmPermission,
                    onNavigateBack = { navController.popBackStack() },
                    scheduleId = scheduleId
                )
            }
        }
    }
}