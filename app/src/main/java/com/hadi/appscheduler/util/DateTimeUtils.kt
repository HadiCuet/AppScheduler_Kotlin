package com.hadi.appscheduler.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    
    fun formatDate(timeMillis: Long): String {
        return dateFormat.format(Date(timeMillis))
    }
    
    fun formatTime(timeMillis: Long): String {
        return timeFormat.format(Date(timeMillis))
    }
    
    fun formatDateTime(timeMillis: Long): String {
        return dateTimeFormat.format(Date(timeMillis))
    }
    
    fun isInPast(timeMillis: Long): Boolean {
        return timeMillis < System.currentTimeMillis()
    }
    
    fun minutesUntil(timeMillis: Long): Long {
        val diff = timeMillis - System.currentTimeMillis()
        return diff / (60 * 1000)
    }
    
    fun hoursUntil(timeMillis: Long): Long {
        val diff = timeMillis - System.currentTimeMillis()
        return diff / (60 * 60 * 1000)
    }
    
    fun getTimeUntilString(timeMillis: Long): String {
        val minutes = minutesUntil(timeMillis)
        return when {
            minutes < 0 -> "Overdue"
            minutes < 60 -> "${minutes}m"
            minutes < 24 * 60 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes / (24 * 60)}d ${(minutes % (24 * 60)) / 60}h"
        }
    }
}