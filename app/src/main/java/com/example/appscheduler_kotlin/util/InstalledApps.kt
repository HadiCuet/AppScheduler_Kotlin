package com.example.appscheduler_kotlin.util

import android.content.Context
import android.content.Intent // Keep for reference, but not directly used in the new logic
import android.content.pm.PackageManager // Added for PackageManager.GET_META_DATA
import android.graphics.drawable.Drawable
import android.util.Log

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object InstalledApps {
    fun loadLaunchable(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        // Get all installed applications
        val allInstalledAppsInfo = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            Log.e("InstalledApps", "Failed to get installed applications", e)
            return emptyList() // Return empty if we can't get the list
        }

        val apps = allInstalledAppsInfo.mapNotNull { appInfo ->
            // Check if the application has a launch intent
            if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                try {
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val icon = pm.getApplicationIcon(appInfo)
                    InstalledApp(appInfo.packageName, label, icon)
                } catch (e: Exception) {
                    // Log error for individual app loading failure but continue with others
                    Log.w("InstalledApps", "Failed to load details for app: ${appInfo.packageName}", e)
                    null
                }
            } else {
                null // Not a launchable app
            }
        }

        // Distinct by package name (though getInstalledApplications should already be distinct)
        // and sort by label
        return apps
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
