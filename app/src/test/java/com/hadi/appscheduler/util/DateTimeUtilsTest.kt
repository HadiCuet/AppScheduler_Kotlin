package com.hadi.appscheduler.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateTimeUtilsTest {
    
    @Test
    fun testFormatDate() {
        val timeMillis = 1640995200000L // January 1, 2022 00:00:00 GMT
        val formatted = DateTimeUtils.formatDate(timeMillis)
        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("2022") || formatted.contains("2021")) // Time zone dependent
    }
    
    @Test
    fun testFormatTime() {
        val timeMillis = 1640995200000L // January 1, 2022 00:00:00 GMT
        val formatted = DateTimeUtils.formatTime(timeMillis)
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
    }
    
    @Test
    fun testFormatDateTime() {
        val timeMillis = 1640995200000L // January 1, 2022 00:00:00 GMT
        val formatted = DateTimeUtils.formatDateTime(timeMillis)
        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("at"))
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
    }
    
    @Test
    fun testIsInPast() {
        val pastTime = System.currentTimeMillis() - 60000 // 1 minute ago
        val futureTime = System.currentTimeMillis() + 60000 // 1 minute from now
        
        assertTrue(DateTimeUtils.isInPast(pastTime))
        assertFalse(DateTimeUtils.isInPast(futureTime))
    }
    
    @Test
    fun testMinutesUntil() {
        val futureTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes from now
        val minutes = DateTimeUtils.minutesUntil(futureTime)
        
        assertTrue(minutes >= 4 && minutes <= 5) // Account for timing variations
    }
    
    @Test
    fun testHoursUntil() {
        val futureTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
        val hours = DateTimeUtils.hoursUntil(futureTime)
        
        assertTrue(hours >= 1 && hours <= 2) // Account for timing variations
    }
    
    @Test
    fun testGetTimeUntilString() {
        val currentTime = System.currentTimeMillis()
        
        // Past time
        val pastTime = currentTime - 60000
        assertEquals("Overdue", DateTimeUtils.getTimeUntilString(pastTime))
        
        // 30 minutes from now
        val thirtyMinutes = currentTime + (30 * 60 * 1000)
        val thirtyMinString = DateTimeUtils.getTimeUntilString(thirtyMinutes)
        assertTrue(thirtyMinString.endsWith("m"))
        
        // 2 hours from now
        val twoHours = currentTime + (2 * 60 * 60 * 1000)
        val twoHourString = DateTimeUtils.getTimeUntilString(twoHours)
        assertTrue(twoHourString.contains("h"))
        
        // 25 hours from now
        val twentyFiveHours = currentTime + (25 * 60 * 60 * 1000)
        val dayString = DateTimeUtils.getTimeUntilString(twentyFiveHours)
        assertTrue(dayString.contains("d"))
    }
}