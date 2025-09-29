package com.example.appscheduler_kotlin.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.ScheduleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A transparent activity started from the notification tap.
 * It marks the schedule as "intent sent", launches the target app, and finishes.
 */
class LaunchProxyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE) ?: ""

        if (scheduleId != -1L && targetPackage.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.get(this@LaunchProxyActivity)
                val s = db.scheduleDao().get(scheduleId)
                if (s != null) {
                    db.scheduleDao().update(
                        s.copy(status = ScheduleStatus.LAUNCH_INTENT_SENT, updatedAt = System.currentTimeMillis())
                    )
                }
            }

            val launch = packageManager.getLaunchIntentForPackage(targetPackage)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            if (launch != null) {
                startActivity(launch)
            }
        }
        finish()
    }

    companion object {
        private const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val EXTRA_PACKAGE = "package"

        fun pendingIntent(context: Context, requestCode: Int, scheduleId: Long, packageName: String): PendingIntent {
            val i = Intent(context, LaunchProxyActivity::class.java).apply {
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(EXTRA_PACKAGE, packageName)
            }
            return PendingIntent.getActivity(
                context,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}