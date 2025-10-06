package com.example.appscheduler_kotlin.repo

import android.content.Context
import android.content.pm.PackageManager
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.Schedule
import com.example.appscheduler_kotlin.data.ScheduleDao
import com.example.appscheduler_kotlin.data.ScheduleStatus
import com.example.appscheduler_kotlin.domain.AppScheduler
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class SchedulesRepository(
    private val context: Context,
    private val dao: ScheduleDao,
    private val scheduler: AppScheduler
) {
    fun observeAll(): Flow<List<Schedule>> = dao.getAllFlow()

    suspend fun create(packageName: String, triggerAt: Long): Result<Long> {
        val appLabel = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to packageName if label not found
        }

        val requestCode = Random.nextInt(100000, 999999)
        val s = Schedule(
            appLabel = appLabel,
            packageName = packageName,
            triggerAtMillis = triggerAt,
            requestCode = requestCode,
            status = ScheduleStatus.SCHEDULED
        )
        return try {
            val id = dao.insert(s)
            // Create a new Schedule instance with the generated id to pass to the scheduler
            val scheduleWithId = s.copy(id = id)
            scheduler.schedule(scheduleWithId)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTime(id: Long, newTime: Long): Result<Unit> {
        val existing = dao.get(id) ?: return Result.failure(IllegalArgumentException("Not found"))
        // Cancel previous alarm
        scheduler.cancel(existing) // Pass the Schedule object
        val updated = existing.copy(
            triggerAtMillis = newTime,
            status = ScheduleStatus.SCHEDULED,
            updatedAt = System.currentTimeMillis()
        )
        return try {
            dao.update(updated)
            scheduler.schedule(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancel(id: Long): Result<Unit> {
        val existing = dao.get(id) ?: return Result.success(Unit) // If not found, nothing to cancel
        scheduler.cancel(existing)
        val updated = existing.copy(
            status = ScheduleStatus.CANCELLED,
            updatedAt = System.currentTimeMillis()
        )
        dao.update(updated)
        return Result.success(Unit)
    }
}

// Factory function needs to pass context to the constructor
fun SchedulesRepository(context: Context): SchedulesRepository {
    val db = AppDatabase.get(context)
    val scheduler = AppScheduler(context)
    // Pass context to the main constructor
    return SchedulesRepository(context.applicationContext, db.scheduleDao(), scheduler)
}
