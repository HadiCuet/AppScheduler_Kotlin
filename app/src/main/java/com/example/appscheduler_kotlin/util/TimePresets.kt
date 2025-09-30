package com.example.appscheduler_kotlin.util

import java.util.Calendar
import kotlin.math.abs

/**
 * Utility object for managing time presets and formatting human-readable time summaries
 */
object TimePresets {
    
    /**
     * Time preset options for quick selection
     */
    enum class Preset(val label: String, val hour: Int, val minute: Int) {
        PLUS_5_MIN("+5 min", -1, -1), // Special case for relative time
        MORNING("Morning 9:00", 9, 0),
        AFTERNOON("Afternoon 13:00", 13, 0),
        EVENING("Evening 18:00", 18, 0),
        TONIGHT("Tonight 21:00", 21, 0)
    }
    
    /**
     * Apply a preset to get the appropriate timestamp
     * @param preset The preset to apply
     * @param baseTime Optional base time (defaults to current time)
     * @return Timestamp in milliseconds
     */
    fun applyPreset(preset: Preset, baseTime: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = baseTime }
        
        return when (preset) {
            Preset.PLUS_5_MIN -> {
                cal.add(Calendar.MINUTE, 5)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> {
                // Set to the target time today
                cal.set(Calendar.HOUR_OF_DAY, preset.hour)
                cal.set(Calendar.MINUTE, preset.minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                
                // If this time has already passed today, schedule for tomorrow
                if (cal.timeInMillis <= System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                cal.timeInMillis
            }
        }
    }
    
    /**
     * Generate a human-friendly summary of when a schedule will fire
     * @param triggerAtMillis The scheduled time in milliseconds
     * @return Human-readable string like "Fires in 1h 12m on Tue, Oct 1"
     */
    fun getHumanFriendlySummary(triggerAtMillis: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = triggerAtMillis - now
        
        if (diffMillis <= 0) {
            return "Time is in the past"
        }
        
        val cal = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue" 
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
        
        val month = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dec"
            else -> ""
        }
        
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        
        // Calculate time difference
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        return when {
            diffDays >= 1 -> {
                val days = diffDays.toInt()
                "Fires in ${days}d on $dayOfWeek, $month $dayOfMonth"
            }
            diffHours >= 1 -> {
                val hours = diffHours.toInt()
                val minutes = (diffMinutes % 60).toInt()
                if (minutes > 0) {
                    "Fires in ${hours}h ${minutes}m on $dayOfWeek, $month $dayOfMonth"
                } else {
                    "Fires in ${hours}h on $dayOfWeek, $month $dayOfMonth"
                }
            }
            diffMinutes >= 1 -> {
                val minutes = diffMinutes.toInt()
                "Fires in ${minutes}m on $dayOfWeek, $month $dayOfMonth"
            }
            else -> {
                "Fires in less than a minute on $dayOfWeek, $month $dayOfMonth"
            }
        }
    }
    
    /**
     * Check if a time is in the past
     */
    fun isPastTime(triggerAtMillis: Long): Boolean {
        return triggerAtMillis <= System.currentTimeMillis()
    }
    
    /**
     * Format time for display in date/time chips
     */
    fun formatTimeForChip(triggerAtMillis: Long?): Pair<String, String> {
        if (triggerAtMillis == null) return "Select Date" to "Select Time"
        
        val cal = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
        
        val month = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dec"
            else -> ""
        }
        
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        
        val dateStr = "$dayOfWeek, $month $dayOfMonth"
        val timeStr = String.format("%02d:%02d", hour, minute)
        
        return dateStr to timeStr
    }
}