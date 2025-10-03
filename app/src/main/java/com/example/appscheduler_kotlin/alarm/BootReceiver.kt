package com.example.appscheduler_kotlin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appscheduler_kotlin.data.ScheduleStatus
import com.example.appscheduler_kotlin.domain.AppScheduler
import com.example.appscheduler_kotlin.repo.SchedulesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = SchedulesRepository(context)
            val scheduler = AppScheduler(context)
            
            // Launch a coroutine to handle re-scheduling off the main thread
            CoroutineScope(Dispatchers.IO).launch {
                val activeSchedules = repository.observeAll().first()
                    .filter { it.status == ScheduleStatus.SCHEDULED }

                activeSchedules.forEach { schedule ->
                    // Re-schedule each active alarm
                    scheduler.schedule(schedule)
                }
            }
        }
    }
}
