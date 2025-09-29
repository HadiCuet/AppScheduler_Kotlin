package com.hadi.appscheduler.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hadi.appscheduler.R
import com.hadi.appscheduler.data.AppDatabase
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.domain.LaunchVerifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "app_scheduler_alarms"
        private const val NOTIFICATION_CHANNEL_NAME = "App Scheduler Alarms"
        private const val NOTIFICATION_ID_BASE = 10000
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", -1L)
        val packageName = intent.getStringExtra("package_name") ?: return
        val appName = intent.getStringExtra("app_name") ?: packageName
        
        if (scheduleId == -1L) return
        
        // Create notification channel if needed
        createNotificationChannel(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val schedule = database.scheduleDao().getScheduleById(scheduleId)
            
            if (schedule != null && schedule.status == ScheduleStatus.SCHEDULED) {
                // Update status to FIRED
                database.scheduleDao().updateScheduleStatus(
                    scheduleId,
                    ScheduleStatus.FIRED,
                    System.currentTimeMillis(),
                    "Alarm fired successfully"
                )
                
                // Show notification
                showLaunchNotification(context, schedule, appName)
                
                // Optional: Start verification process
                startVerificationProcess(context, schedule)
            }
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled app launches"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showLaunchNotification(context: Context, schedule: Schedule, appName: String) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(schedule.packageName)
        
        if (launchIntent == null) {
            // App might have been uninstalled
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                database.scheduleDao().updateScheduleStatus(
                    schedule.id,
                    ScheduleStatus.MISSED,
                    System.currentTimeMillis(),
                    "App not found or uninstalled"
                )
            }
            return
        }
        
        // Create launch pending intent
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            schedule.requestCode + 1000, // Offset to avoid conflicts
            launchIntent.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("launched_by_scheduler", true)
                putExtra("schedule_id", schedule.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("Time to open $appName")
            .setContentText("Tap to launch the app")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(launchPendingIntent)
            .addAction(
                R.drawable.ic_launch,
                "Launch App",
                launchPendingIntent
            )
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + schedule.requestCode,
                notification
            )
            
            // Update status to show notification was sent
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                database.scheduleDao().updateScheduleStatus(
                    schedule.id,
                    ScheduleStatus.LAUNCH_INTENT_SENT,
                    System.currentTimeMillis(),
                    "Notification sent to user"
                )
            }
        } catch (e: SecurityException) {
            // Notification permission might be denied
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                database.scheduleDao().updateScheduleStatus(
                    schedule.id,
                    ScheduleStatus.MISSED,
                    System.currentTimeMillis(),
                    "Notification permission denied"
                )
            }
        }
    }
    
    private fun startVerificationProcess(context: Context, schedule: Schedule) {
        CoroutineScope(Dispatchers.IO).launch {
            val verifier = LaunchVerifier(context)
            
            if (!verifier.hasUsageStatsPermission()) {
                return@launch // Skip verification if no permission
            }
            
            // Wait a bit for the user to potentially tap the notification
            kotlinx.coroutines.delay(2000)
            
            val launched = verifier.verifyAppLaunched(schedule.packageName, System.currentTimeMillis())
            
            val database = AppDatabase.getDatabase(context)
            if (launched) {
                database.scheduleDao().updateScheduleWithVerification(
                    schedule.id,
                    ScheduleStatus.LAUNCHED_CONFIRMED,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    "App launch verified"
                )
            } else {
                // Keep as LAUNCH_INTENT_SENT since we sent the notification but can't verify launch
                database.scheduleDao().updateScheduleWithVerification(
                    schedule.id,
                    ScheduleStatus.LAUNCH_INTENT_SENT,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    "Notification sent, launch not verified"
                )
            }
        }
    }
}