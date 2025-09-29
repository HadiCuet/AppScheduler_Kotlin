package com.example.appscheduler_kotlin.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

object PermissionHelpers {
    fun canPostNotifications(): Boolean {
        return true
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(AlarmManager::class.java)
            am.canScheduleExactAlarms()
        } else true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun openExactAlarmSettings(context: Context) {
        val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}