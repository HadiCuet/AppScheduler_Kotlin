package com.example.appscheduler_kotlin

import android.app.Application
import com.example.appscheduler_kotlin.util.Notifications

class AppSchedulerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannel(this)
    }
}