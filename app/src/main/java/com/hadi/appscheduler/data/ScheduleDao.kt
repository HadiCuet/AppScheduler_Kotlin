package com.hadi.appscheduler.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    
    @Query("SELECT * FROM schedules ORDER BY triggerAtMillis ASC")
    fun getAllSchedules(): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE status = :status ORDER BY triggerAtMillis ASC")
    fun getSchedulesByStatus(status: ScheduleStatus): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?
    
    @Query("SELECT * FROM schedules WHERE requestCode = :requestCode")
    suspend fun getScheduleByRequestCode(requestCode: Int): Schedule?
    
    @Query("SELECT * FROM schedules WHERE status = 'SCHEDULED' AND triggerAtMillis > :currentTime ORDER BY triggerAtMillis ASC")
    suspend fun getUpcomingSchedules(currentTime: Long = System.currentTimeMillis()): List<Schedule>
    
    @Query("SELECT COUNT(*) FROM schedules WHERE triggerAtMillis = :triggerTime AND status = 'SCHEDULED'")
    suspend fun getScheduleCountAtTime(triggerTime: Long): Int
    
    @Query("SELECT * FROM schedules WHERE status IN ('FIRED', 'LAUNCH_INTENT_SENT', 'LAUNCHED_CONFIRMED', 'MISSED', 'CANCELLED') ORDER BY updatedAt DESC")
    fun getExecutionLog(): Flow<List<Schedule>>
    
    @Insert
    suspend fun insertSchedule(schedule: Schedule): Long
    
    @Update
    suspend fun updateSchedule(schedule: Schedule)
    
    @Delete
    suspend fun deleteSchedule(schedule: Schedule)
    
    @Query("UPDATE schedules SET status = :status, updatedAt = :updatedAt, resultMessage = :resultMessage WHERE id = :id")
    suspend fun updateScheduleStatus(id: Long, status: ScheduleStatus, updatedAt: Long, resultMessage: String? = null)
    
    @Query("UPDATE schedules SET status = :status, updatedAt = :updatedAt, lastVerificationAt = :verificationAt, resultMessage = :resultMessage WHERE id = :id")
    suspend fun updateScheduleWithVerification(id: Long, status: ScheduleStatus, updatedAt: Long, verificationAt: Long, resultMessage: String? = null)
}