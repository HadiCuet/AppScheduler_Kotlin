package com.example.appscheduler_kotlin.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleStatus { SCHEDULED, FIRED, LAUNCH_INTENT_SENT, CANCELLED, MISSED }

@Entity(
    tableName = "schedules",
    indices = [
        Index(value = ["triggerAtMillis"], unique = true) // prevent exact-time conflicts
    ]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val triggerAtMillis: Long,
    val requestCode: Int,
    val status: ScheduleStatus = ScheduleStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resultMessage: String? = null
)