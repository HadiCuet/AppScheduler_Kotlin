package com.example.appscheduler_kotlin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.ScheduleStatus // This import is necessary for ScheduleStatus.SCHEDULED
import com.example.appscheduler_kotlin.domain.AppScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.get(context)
            // Corrected DAO call
            val upcoming = db.scheduleDao().upcoming(
                now = System.currentTimeMillis(),
                status = ScheduleStatus.SCHEDULED
            )
            val scheduler = AppScheduler(context)
            upcoming.forEach {
                scheduler.schedule(it)
            }
            pending.finish()
        }
    }
}
