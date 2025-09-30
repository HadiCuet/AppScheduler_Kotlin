package com.example.appscheduler_kotlin.util

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

/**
 * Unit tests for TimePresets utility class
 */
class TimePresetsTest {

    @Test
    fun `applyPreset PLUS_5_MIN adds 5 minutes to base time`() {
        val baseTime = System.currentTimeMillis()
        val result = TimePresets.applyPreset(TimePresets.Preset.PLUS_5_MIN, baseTime)
        
        val expectedTime = Calendar.getInstance().apply {
            timeInMillis = baseTime
            add(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        assertEquals(expectedTime, result)
    }

    @Test
    fun `applyPreset MORNING sets time to 9_00 today or tomorrow`() {
        val morning9 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 9:00 has passed today, it should be tomorrow
        if (morning9.timeInMillis <= System.currentTimeMillis()) {
            morning9.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val result = TimePresets.applyPreset(TimePresets.Preset.MORNING)
        assertEquals(morning9.timeInMillis, result)
    }

    @Test
    fun `applyPreset EVENING sets time to 18_00 today or tomorrow`() {
        val evening18 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 18:00 has passed today, it should be tomorrow
        if (evening18.timeInMillis <= System.currentTimeMillis()) {
            evening18.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val result = TimePresets.applyPreset(TimePresets.Preset.EVENING)
        assertEquals(evening18.timeInMillis, result)
    }

    @Test
    fun `isPastTime correctly identifies past times`() {
        val pastTime = System.currentTimeMillis() - 60000 // 1 minute ago
        val futureTime = System.currentTimeMillis() + 60000 // 1 minute from now
        
        assertTrue(TimePresets.isPastTime(pastTime))
        assertFalse(TimePresets.isPastTime(futureTime))
    }

    @Test
    fun `getHumanFriendlySummary formats minutes correctly`() {
        val futureTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes from now
        val summary = TimePresets.getHumanFriendlySummary(futureTime)
        
        assertTrue(summary.contains("Fires in 5m"))
        assertTrue(summary.contains("on"))
    }

    @Test
    fun `getHumanFriendlySummary formats hours and minutes correctly`() {
        val futureTime = System.currentTimeMillis() + (75 * 60 * 1000) // 1 hour 15 minutes from now
        val summary = TimePresets.getHumanFriendlySummary(futureTime)
        
        assertTrue(summary.contains("Fires in 1h 15m"))
        assertTrue(summary.contains("on"))
    }

    @Test
    fun `getHumanFriendlySummary handles past time`() {
        val pastTime = System.currentTimeMillis() - 60000 // 1 minute ago
        val summary = TimePresets.getHumanFriendlySummary(pastTime)
        
        assertEquals("Time is in the past", summary)
    }

    @Test
    fun `formatTimeForChip returns correct format`() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.OCTOBER, 15, 14, 30, 0) // Oct 15, 2024, 14:30
        }
        
        val (dateStr, timeStr) = TimePresets.formatTimeForChip(calendar.timeInMillis)
        
        assertEquals("Tue, Oct 15", dateStr)
        assertEquals("14:30", timeStr)
    }

    @Test
    fun `formatTimeForChip handles null input`() {
        val (dateStr, timeStr) = TimePresets.formatTimeForChip(null)
        
        assertEquals("Select Date", dateStr)
        assertEquals("Select Time", timeStr)
    }

    @Test
    fun `all presets have valid times`() {
        TimePresets.Preset.values().forEach { preset ->
            if (preset != TimePresets.Preset.PLUS_5_MIN) {
                assertTrue("Preset ${preset.name} should have valid hour", preset.hour in 0..23)
                assertTrue("Preset ${preset.name} should have valid minute", preset.minute in 0..59)
            }
        }
    }

    @Test
    fun `applyPreset ensures future time for all fixed time presets`() {
        val fixedTimePresets = TimePresets.Preset.values().filter { it != TimePresets.Preset.PLUS_5_MIN }
        
        fixedTimePresets.forEach { preset ->
            val result = TimePresets.applyPreset(preset)
            assertTrue("Preset ${preset.name} should always return a future time", result > System.currentTimeMillis())
        }
    }
}