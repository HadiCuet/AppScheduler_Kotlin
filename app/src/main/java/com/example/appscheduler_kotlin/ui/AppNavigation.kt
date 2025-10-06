package com.example.appscheduler_kotlin.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appscheduler_kotlin.ui.screens.EditScheduleScreen
import com.example.appscheduler_kotlin.ui.screens.ScheduleListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppDestinations.SCHEDULE_LIST_ROUTE) {
        composable(AppDestinations.SCHEDULE_LIST_ROUTE) {
            ScheduleListScreen(
                onCreateNew = { navController.navigate(AppDestinations.EDIT_SCHEDULE_ROUTE_BASE + "/null") },
                onEdit = { scheduleId -> navController.navigate(AppDestinations.EDIT_SCHEDULE_ROUTE_BASE + "/$scheduleId") }
            )
        }
        composable(AppDestinations.EDIT_SCHEDULE_ROUTE) { backStackEntry ->
            val scheduleIdStr = backStackEntry.arguments?.getString(AppDestinations.SCHEDULE_ID_KEY)
            val scheduleId = scheduleIdStr?.takeIf { it != "null" }?.toLongOrNull()
            EditScheduleScreen(
                scheduleId = scheduleId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}

object AppDestinations {
    const val SCHEDULE_LIST_ROUTE = "schedule_list"
    const val SCHEDULE_ID_KEY = "scheduleId"
    const val EDIT_SCHEDULE_ROUTE_BASE = "edit_schedule"
    const val EDIT_SCHEDULE_ROUTE = "$EDIT_SCHEDULE_ROUTE_BASE/{$SCHEDULE_ID_KEY}"
}
