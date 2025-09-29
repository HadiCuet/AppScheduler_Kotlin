package com.hadi.appscheduler.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hadi.appscheduler.alarm.AlarmReceiver
import com.hadi.appscheduler.data.Schedule
import kotlin.random.Random

class AppScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleAlarm(schedule: Schedule): Boolean {
        return try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("schedule_id", schedule.id)
                putExtra("package_name", schedule.packageName)
                putExtra("app_name", schedule.appName)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Use setExactAndAllowWhileIdle for exact timing even in Doze mode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                schedule.triggerAtMillis,
                pendingIntent
            )
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun cancelAlarm(schedule: Schedule): Boolean {
        return try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun generateRequestCode(): Int {
        // Generate a stable request code that can be used for PendingIntent
        return Random.nextInt(1000, Int.MAX_VALUE)
    }
    
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}