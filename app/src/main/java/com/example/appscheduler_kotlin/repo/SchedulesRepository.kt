package com.example.appscheduler_kotlin.repo

import android.content.Context
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.Schedule
import com.example.appscheduler_kotlin.data.ScheduleDao
import com.example.appscheduler_kotlin.data.ScheduleStatus
import com.example.appscheduler_kotlin.domain.AppScheduler
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class SchedulesRepository(
    private val dao: ScheduleDao,
    private val scheduler: AppScheduler
) {
    fun observeAll(): Flow<List<Schedule>> = dao.getAllFlow()

    suspend fun create(packageName: String, triggerAt: Long): Result<Long> {
        val requestCode = Random.nextInt(100000, 999999)
        val s = Schedule(
            packageName = packageName,
            triggerAtMillis = triggerAt,
            requestCode = requestCode,
            status = ScheduleStatus.SCHEDULED
        )
        return try {
            val id = dao.insert(s)
            scheduler.scheduleExact(id, requestCode, triggerAt)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTime(id: Long, newTime: Long): Result<Unit> {
        val existing = dao.get(id) ?: return Result.failure(IllegalArgumentException("Not found"))
        // cancel previous alarm
        scheduler.cancel(existing.requestCode)
        val updated = existing.copy(
            triggerAtMillis = newTime,
            status = ScheduleStatus.SCHEDULED,
            updatedAt = System.currentTimeMillis()
        )
        return try {
            dao.update(updated)
            scheduler.scheduleExact(updated.id, updated.requestCode, newTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancel(id: Long): Result<Unit> {
        val existing = dao.get(id) ?: return Result.success(Unit)
        scheduler.cancel(existing.requestCode)
        val updated = existing.copy(
            status = ScheduleStatus.CANCELLED,
            updatedAt = System.currentTimeMillis()
        )
        dao.update(updated)
        return Result.success(Unit)
    }
}

fun SchedulesRepository(context: Context): SchedulesRepository {
    val db = AppDatabase.get(context)
    val scheduler = AppScheduler(context)
    return SchedulesRepository(db.scheduleDao(), scheduler)
}