package com.hadi.appscheduler.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hadi.appscheduler.data.AppDatabase
import com.hadi.appscheduler.domain.AppScheduler
import com.hadi.appscheduler.ui.theme.AppSchedulerTheme
import com.hadi.appscheduler.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                // Handle notification permission denied
                // Could show a dialog explaining why the permission is needed
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        val appScheduler = AppScheduler(this)
        val repository = ScheduleRepository(database.scheduleDao(), appScheduler)
        val appListProvider = AppListProvider(packageManager)

        setContent {
            AppSchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppSchedulerApp(
                        repository = repository,
                        appListProvider = appListProvider,
                        onRequestExactAlarmPermission = ::requestExactAlarmPermission,
                        onRequestUsageStatsPermission = ::requestUsageStatsPermission
                    )
                }
            }
        }
    }
    
    private fun requestExactAlarmPermission() {
        try {
            startActivity(PermissionHelper.getExactAlarmSettingsIntent(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun requestUsageStatsPermission() {
        try {
            startActivity(PermissionHelper.getUsageStatsSettingsIntent())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}