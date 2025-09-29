package com.example.appscheduler_kotlin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.Schedule
import com.example.appscheduler_kotlin.data.ScheduleStatus
import com.example.appscheduler_kotlin.domain.AppScheduler // Import AppScheduler for its constants
import com.example.appscheduler_kotlin.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Use the constant from AppScheduler for the extra key
        val scheduleId = intent.getLongExtra(AppScheduler.EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId == -1L) {
            println("AlarmReceiver: Received invalid scheduleId, aborting.") // Added log
            return
        }
        println("AlarmReceiver: Received scheduleId: $scheduleId") // Added log

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.get(context)
            val s = db.scheduleDao().get(scheduleId)
            if (s == null) {
                println("AlarmReceiver: Schedule not found in DB for id: $scheduleId") // Added log
                pending.finish()
                return@launch
            }

            // Optional: Check if the schedule should still fire (e.g., status is SCHEDULED)
            if (s.status != ScheduleStatus.SCHEDULED) {
                println("AlarmReceiver: Schedule $scheduleId status is ${s.status}, not firing notification.") // Added log
                pending.finish()
                return@launch
            }

            db.scheduleDao().update(
                s.copy(status = ScheduleStatus.FIRED, updatedAt = System.currentTimeMillis())
            )
            showNotification(context, s)
            println("AlarmReceiver: Notification processed for scheduleId: $scheduleId") // Added log
            pending.finish()
        }
    }

    private fun showNotification(context: Context, sched: Schedule) {
        Notifications.ensureChannel(context)

        val contentIntent = LaunchProxyActivity.pendingIntent(
            context = context,
            requestCode = sched.requestCode,
            scheduleId = sched.id,
            packageName = sched.packageName
        )

        val appLabel = sched.appLabelOrPackage(context)
        val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.noti_title, appLabel))
            .setContentText(context.getString(R.string.noti_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(sched.requestCode, notification)
        println("AlarmReceiver: Notification shown for $appLabel (ID: ${sched.id})") // Added log
    }
}

private fun Schedule.appLabelOrPackage(context: Context): String {
    return try {
        val pm = context.packageManager
        val ai = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(ai).toString()
    } catch (e: Exception) {
        packageName
    }
}
