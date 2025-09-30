@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appscheduler_kotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appscheduler_kotlin.ui.theme.AppScheduler_KotlinTheme
import com.example.appscheduler_kotlin.ui.screens.AppSelectionCard
import com.example.appscheduler_kotlin.util.TimePresets

/**
 * Preview components for the redesigned Material 3 edit schedule screen
 */

@Preview(name = "App Selection Card - Empty", showBackground = true)
@Composable
fun AppSelectionCardEmptyPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            AppSelectionCard(
                selectedApp = null to null,
                onClick = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "App Selection Card - Selected", showBackground = true)
@Composable
fun AppSelectionCardSelectedPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            AppSelectionCard(
                selectedApp = "com.android.chrome" to "Chrome",
                onClick = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Date Time Section - Empty", showBackground = true)
@Composable
fun DateTimeSectionEmptyPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            DateTimeSection(
                triggerAtMillis = null,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Date Time Section - Selected", showBackground = true)
@Composable
fun DateTimeSectionSelectedPreview() {
    val futureTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
    
    AppScheduler_KotlinTheme {
        Surface {
            DateTimeSection(
                triggerAtMillis = futureTime,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Date Time Section - Error", showBackground = true)
@Composable
fun DateTimeSectionErrorPreview() {
    val pastTime = System.currentTimeMillis() - (60 * 1000) // 1 minute ago
    
    AppScheduler_KotlinTheme {
        Surface {
            DateTimeSection(
                triggerAtMillis = pastTime,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { },
                hasConflict = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Date Time Section - Conflict", showBackground = true)
@Composable
fun DateTimeSectionConflictPreview() {
    val futureTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
    
    AppScheduler_KotlinTheme {
        Surface {
            DateTimeSection(
                triggerAtMillis = futureTime,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { },
                hasConflict = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Bottom Action Bar - Enabled", showBackground = true)
@Composable
fun BottomActionBarEnabledPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            BottomActionBar(
                onSave = { },
                onCancel = { },
                saveEnabled = true,
                isLoading = false
            )
        }
    }
}

@Preview(name = "Bottom Action Bar - Disabled", showBackground = true)
@Composable
fun BottomActionBarDisabledPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            BottomActionBar(
                onSave = { },
                onCancel = { },
                saveEnabled = false,
                isLoading = false
            )
        }
    }
}

@Preview(name = "Bottom Action Bar - Loading", showBackground = true)
@Composable
fun BottomActionBarLoadingPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            BottomActionBar(
                onSave = { },
                onCancel = { },
                saveEnabled = true,
                isLoading = true
            )
        }
    }
}

@Preview(name = "Permission Banner", showBackground = true)
@Composable
fun PermissionBannerPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            InfoBanner(
                title = "Exact Alarm Permission Needed",
                message = "To schedule apps at precise times, please allow exact alarms in settings.",
                actionText = "Open Settings",
                onActionClick = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Error Banner", showBackground = true)
@Composable
fun ErrorBannerPreview() {
    AppScheduler_KotlinTheme {
        Surface {
            ErrorBanner(
                title = "Scheduling Error",
                message = "A schedule already exists at this time. Please pick a different time.",
                actionText = "Retry",
                onActionClick = { },
                onDismiss = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Full Edit Screen - Light", showBackground = true, heightDp = 800)
@Composable
fun EditScreenLightPreview() {
    AppScheduler_KotlinTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission Banner
                InfoBanner(
                    title = "Exact Alarm Permission Needed",
                    message = "To schedule apps at precise times, please allow exact alarms in settings.",
                    actionText = "Open Settings",
                    onActionClick = { }
                )
                
                // App Selection
                AppSelectionCard(
                    selectedApp = "com.android.chrome" to "Chrome",
                    onClick = { }
                )
                
                // Date & Time
                val futureTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000)
                DateTimeSection(
                    triggerAtMillis = futureTime,
                    onPresetSelected = { },
                    onDateClick = { },
                    onTimeClick = { }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Bottom Bar
                BottomActionBar(
                    onSave = { },
                    onCancel = { },
                    saveEnabled = true
                )
            }
        }
    }
}

@Preview(name = "Full Edit Screen - Dark", showBackground = true, heightDp = 800)
@Composable
fun EditScreenDarkPreview() {
    AppScheduler_KotlinTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Selection - Empty State
                AppSelectionCard(
                    selectedApp = null to null,
                    onClick = { }
                )
                
                // Date & Time - Error State
                val pastTime = System.currentTimeMillis() - (60 * 1000)
                DateTimeSection(
                    triggerAtMillis = pastTime,
                    onPresetSelected = { },
                    onDateClick = { },
                    onTimeClick = { }
                )
                
                // Error Banner
                ErrorBanner(
                    title = "Scheduling Error",
                    message = "Time must be in the future.",
                    onDismiss = { }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Bottom Bar - Disabled
                BottomActionBar(
                    onSave = { },
                    onCancel = { },
                    saveEnabled = false
                )
            }
        }
    }
}