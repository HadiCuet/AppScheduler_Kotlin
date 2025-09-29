package com.example.appscheduler_kotlin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.example.appscheduler_kotlin.R

object Notifications {
    const val CHANNEL_ID = "schedule_channel"

    fun ensureChannel(context: Context) {
        val nm: NotificationManager? = context.getSystemService()
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_desc)
        }
        nm?.createNotificationChannel(channel)
    }
}