package com.example.appscheduler_kotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun get(id: Long): Schedule?

    @Query("SELECT * FROM schedules ORDER BY triggerAtMillis DESC")
    fun getAllFlow(): Flow<List<Schedule>>

    @Query("""
        SELECT * FROM schedules 
        WHERE triggerAtMillis > :now AND status = :status 
        ORDER BY triggerAtMillis ASC
    """)
    suspend fun upcoming(now: Long, status: ScheduleStatus): List<Schedule>
}