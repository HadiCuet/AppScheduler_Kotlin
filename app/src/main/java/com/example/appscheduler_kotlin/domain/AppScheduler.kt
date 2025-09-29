package com.example.appscheduler_kotlin.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appscheduler_kotlin.alarm.AlarmReceiver
import com.example.appscheduler_kotlin.data.Schedule
import java.util.concurrent.TimeUnit

class AppScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: Schedule) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            // Adding package name for logging or quick access in receiver, though ID is primary
            putExtra(EXTRA_PACKAGE_NAME, schedule.packageName)
            // Adding trigger time for logging or verification
            putExtra(EXTRA_TRIGGER_AT_MILLIS, schedule.triggerAtMillis)
        }

        // Using schedule.id as the requestCode for PendingIntent uniqueness per schedule
        // Flag_IMMUTABLE is required for target SDK 31+
        // Flag_UPDATE_CURRENT will update the existing PendingIntent if one with the same
        // request code exists, which is useful if a schedule is updated.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.requestCode, // Using the stable requestCode from the Schedule entity
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure the app has permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Handle cases where permission is not granted.
                // This might involve logging, updating schedule status to MISSED,
                // or notifying the user to grant permission.
                // For now, we'll just log or skip scheduling.
                println("Cannot schedule exact alarms. Permission not granted.")
                // Optionally, update schedule status or throw an exception
                return
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                schedule.triggerAtMillis,
                pendingIntent
            )
        } catch (se: SecurityException) {
            // Handle cases where SCHEDULE_EXACT_ALARM permission might be revoked at runtime
            // or other security issues.
            println("SecurityException while scheduling alarm: ${se.message}")
            // Optionally, update schedule status or re-throw
        }
    }

    fun cancel(schedule: Schedule) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE // Action must match the one used for scheduling
        }

        // Recreate the same PendingIntent used for scheduling to cancel it.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.requestCode, // Using the stable requestCode
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE to avoid creating a new one if it doesn't exist
        )

        // pendingIntent will be null if no PendingIntent matched the criteria with FLAG_NO_CREATE
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel() // Also cancel the PendingIntent itself
        }
    }

    companion object {
        const val ACTION_FIRE = "com.example.appscheduler_kotlin.ACTION_FIRE_ALARM"
        const val EXTRA_SCHEDULE_ID = "com.example.appscheduler_kotlin.EXTRA_SCHEDULE_ID"
        const val EXTRA_PACKAGE_NAME = "com.example.appscheduler_kotlin.EXTRA_PACKAGE_NAME"
        const val EXTRA_TRIGGER_AT_MILLIS = "com.example.appscheduler_kotlin.EXTRA_TRIGGER_AT_MILLIS"
    }
}
