package com.hadi.appscheduler.data

enum class ScheduleStatus {
    SCHEDULED,
    FIRED,
    LAUNCH_INTENT_SENT,
    LAUNCHED_CONFIRMED,
    CANCELLED,
    MISSED
}