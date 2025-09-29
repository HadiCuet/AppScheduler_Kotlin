package com.example.appscheduler_kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.appscheduler_kotlin.ui.theme.AppScheduler_KotlinTheme
import com.example.appscheduler_kotlin.ui.AppNavigation // <-- ADD THIS IMPORT

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScheduler_KotlinTheme {
                // --- Notification Permission Request (Android 13+) ---
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val context = LocalContext.current
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted: Boolean ->
                            if (isGranted) {
                                // Permission granted.
                                println("POST_NOTIFICATIONS permission GRANTED")
                            } else {
                                // Permission denied.
                                println("POST_NOTIFICATIONS permission DENIED")
                            }
                        }
                    )

                    LaunchedEffect(key1 = Unit) { // Runs once when the Composable enters the composition
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
                // --- End Notification Permission Request ---

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
