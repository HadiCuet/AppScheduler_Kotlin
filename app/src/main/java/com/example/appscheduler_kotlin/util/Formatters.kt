package com.example.appscheduler_kotlin.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Formatters {
    private val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    fun formatDateTime(millis: Long): String = dateTime.format(Date(millis))
}