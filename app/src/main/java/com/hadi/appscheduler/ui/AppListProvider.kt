package com.hadi.appscheduler.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)

class AppListProvider(private val packageManager: PackageManager) {
    
    fun getInstalledLaunchableApps(): List<AppInfo> {
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
            intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            
            resolveInfos.mapNotNull { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    
                    // Skip system apps (optional - can be configurable)
                    if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        return@mapNotNull null
                    }
                    
                    AppInfo(
                        packageName = packageName,
                        appName = appInfo.loadLabel(packageManager).toString(),
                        icon = appInfo.loadIcon(packageManager)
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.appName }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getAllInstalledApps(): List<AppInfo> {
        return try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps.mapNotNull { appInfo ->
                try {
                    // Only include apps that have a launch intent
                    val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null) {
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = appInfo.loadLabel(packageManager).toString(),
                            icon = appInfo.loadIcon(packageManager)
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.appName }
        } catch (e: Exception) {
            emptyList()
        }
    }
}