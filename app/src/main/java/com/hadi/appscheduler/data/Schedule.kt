package com.hadi.appscheduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val triggerAtMillis: Long,
    val requestCode: Int,
    val status: ScheduleStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val lastVerificationAt: Long? = null,
    val resultMessage: String? = null
)