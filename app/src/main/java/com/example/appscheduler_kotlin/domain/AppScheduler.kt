package com.example.appscheduler_kotlin.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appscheduler_kotlin.alarm.AlarmReceiver

class AppScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExact(scheduleId: Long, requestCode: Int, atMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.appscheduler_kotlin.ACTION_FIRE"
            putExtra("schedule_id", scheduleId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, atMillis, pi)
        }
    }

    fun cancel(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.appscheduler_kotlin.ACTION_FIRE"
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pi != null) alarmManager.cancel(pi)
    }
}