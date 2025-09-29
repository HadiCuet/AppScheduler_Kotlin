package com.example.appscheduler_kotlin.util

import android.content.Context
import android.content.Intent
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
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activities = pm.queryIntentActivities(intent, 0)
        val apps = activities.mapNotNull { ri ->
            try {
                val appInfo = ri.activityInfo.applicationInfo
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                InstalledApp(appInfo.packageName, label, icon)
            } catch (e: Exception) {
                Log.w("InstalledApps", "Failed to read app: ${ri.activityInfo.packageName}", e)
                null
            }
        }.distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
        return apps
    }
}