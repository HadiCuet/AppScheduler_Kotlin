package com.example.appscheduler_kotlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appscheduler_kotlin.ui.screens.EditScheduleScreen
import com.example.appscheduler_kotlin.ui.screens.ScheduleListScreen

sealed interface Route {
    data object List : Route { const val path = "list" }
    data object EditNew : Route { const val path = "edit" }
    data object EditExisting : Route { const val path = "edit/{id}" }
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.List.path) {
        composable(Route.List.path) {
            ScheduleListScreen(
                onCreateNew = { nav.navigate(Route.EditNew.path) },
                onEdit = { id -> nav.navigate("edit/$id") }
            )
        }
        composable(Route.EditNew.path) {
            EditScheduleScreen(scheduleId = null, onDone = { nav.popBackStack() })
        }
        composable(
            route = Route.EditExisting.path,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            val id = it.arguments?.getLong("id")
            EditScheduleScreen(scheduleId = id, onDone = { nav.popBackStack() })
        }
    }
}