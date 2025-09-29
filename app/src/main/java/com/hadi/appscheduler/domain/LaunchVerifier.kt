package com.hadi.appscheduler.domain

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class LaunchVerifier(private val context: Context) {
    
    private val usageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    } else null
    
    suspend fun verifyAppLaunched(packageName: String, launchTimeMillis: Long, timeoutSeconds: Int = 20): Boolean {
        if (usageStatsManager == null) return false
        
        val endTime = System.currentTimeMillis()
        val startTime = launchTimeMillis
        
        // Poll for up to timeoutSeconds to check if the app came to foreground
        repeat(timeoutSeconds) {
            try {
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    startTime,
                    endTime
                )
                
                usageStats?.find { it.packageName == packageName }?.let { stats ->
                    // Check if the app was used recently (within our window)
                    if (stats.lastTimeUsed > launchTimeMillis) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Security exception or other issues accessing usage stats
                e.printStackTrace()
            }
            
            delay(1000) // Wait 1 second before checking again
        }
        
        return false
    }
    
    fun hasUsageStatsPermission(): Boolean {
        if (usageStatsManager == null) return false
        
        return try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.MINUTES.toMillis(1)
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            )
            
            usageStats != null && usageStats.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}