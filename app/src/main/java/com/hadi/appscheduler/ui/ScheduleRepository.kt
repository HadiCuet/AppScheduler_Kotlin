package com.hadi.appscheduler.ui

import com.hadi.appscheduler.data.AppDatabase
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleDao
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.domain.AppScheduler
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class ScheduleRepository(
    private val scheduleDao: ScheduleDao,
    private val appScheduler: AppScheduler
) {
    
    fun getAllSchedules(): Flow<List<Schedule>> = scheduleDao.getAllSchedules()
    
    fun getExecutionLogs(): Flow<List<Schedule>> = scheduleDao.getExecutionLog()
    
    suspend fun getScheduleById(id: Long): Schedule? = scheduleDao.getScheduleById(id)
    
    suspend fun createSchedule(
        packageName: String,
        appName: String,
        triggerAtMillis: Long
    ): Result<Long> {
        return try {
            // Check for time conflicts
            val conflictCount = scheduleDao.getScheduleCountAtTime(triggerAtMillis)
            if (conflictCount > 0) {
                return Result.failure(Exception("Schedule conflict at this time"))
            }
            
            val requestCode = appScheduler.generateRequestCode()
            val schedule = Schedule(
                packageName = packageName,
                appName = appName,
                triggerAtMillis = triggerAtMillis,
                requestCode = requestCode,
                status = ScheduleStatus.SCHEDULED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val scheduleId = scheduleDao.insertSchedule(schedule)
            val savedSchedule = schedule.copy(id = scheduleId)
            
            if (appScheduler.scheduleAlarm(savedSchedule)) {
                Result.success(scheduleId)
            } else {
                // Remove the schedule if we couldn't set the alarm
                scheduleDao.deleteSchedule(savedSchedule)
                Result.failure(Exception("Failed to schedule alarm"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSchedule(
        scheduleId: Long,
        newTriggerAtMillis: Long
    ): Result<Unit> {
        return try {
            val existingSchedule = scheduleDao.getScheduleById(scheduleId)
                ?: return Result.failure(Exception("Schedule not found"))
            
            // Check for time conflicts (excluding current schedule)
            val conflictCount = scheduleDao.getScheduleCountAtTime(newTriggerAtMillis)
            if (conflictCount > 0 && existingSchedule.triggerAtMillis != newTriggerAtMillis) {
                return Result.failure(Exception("Schedule conflict at this time"))
            }
            
            // Cancel existing alarm
            appScheduler.cancelAlarm(existingSchedule)
            
            // Update schedule
            val updatedSchedule = existingSchedule.copy(
                triggerAtMillis = newTriggerAtMillis,
                updatedAt = System.currentTimeMillis()
            )
            
            scheduleDao.updateSchedule(updatedSchedule)
            
            // Set new alarm
            if (appScheduler.scheduleAlarm(updatedSchedule)) {
                Result.success(Unit)
            } else {
                // Restore original alarm if new one failed
                appScheduler.scheduleAlarm(existingSchedule)
                Result.failure(Exception("Failed to update alarm"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelSchedule(scheduleId: Long): Result<Unit> {
        return try {
            val schedule = scheduleDao.getScheduleById(scheduleId)
                ?: return Result.failure(Exception("Schedule not found"))
            
            appScheduler.cancelAlarm(schedule)
            
            scheduleDao.updateScheduleStatus(
                scheduleId,
                ScheduleStatus.CANCELLED,
                System.currentTimeMillis(),
                "Cancelled by user"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSchedule(scheduleId: Long): Result<Unit> {
        return try {
            val schedule = scheduleDao.getScheduleById(scheduleId)
                ?: return Result.failure(Exception("Schedule not found"))
            
            appScheduler.cancelAlarm(schedule)
            scheduleDao.deleteSchedule(schedule)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}