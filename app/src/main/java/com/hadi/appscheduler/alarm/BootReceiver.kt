package com.hadi.appscheduler.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hadi.appscheduler.data.AppDatabase
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.domain.AppScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            
            // Re-schedule all upcoming scheduled alarms
            CoroutineScope(Dispatchers.IO).launch {
                restoreScheduledAlarms(context)
            }
        }
    }
    
    private suspend fun restoreScheduledAlarms(context: Context) {
        try {
            val database = AppDatabase.getDatabase(context)
            val appScheduler = AppScheduler(context)
            
            // Get all upcoming scheduled alarms
            val upcomingSchedules = database.scheduleDao().getUpcomingSchedules(System.currentTimeMillis())
            
            var restoredCount = 0
            var failedCount = 0
            
            for (schedule in upcomingSchedules) {
                if (appScheduler.scheduleAlarm(schedule)) {
                    restoredCount++
                } else {
                    failedCount++
                    // Mark as missed if we can't restore
                    database.scheduleDao().updateScheduleStatus(
                        schedule.id,
                        ScheduleStatus.MISSED,
                        System.currentTimeMillis(),
                        "Failed to restore alarm after boot"
                    )
                }
            }
            
            android.util.Log.d(
                "BootReceiver", 
                "Restored $restoredCount alarms, failed to restore $failedCount alarms"
            )
            
        } catch (e: Exception) {
            android.util.Log.e("BootReceiver", "Error restoring alarms after boot", e)
        }
    }
}