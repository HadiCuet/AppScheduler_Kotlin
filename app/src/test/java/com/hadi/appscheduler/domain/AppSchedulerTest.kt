package com.hadi.appscheduler.domain

import android.content.Context
import android.app.AlarmManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [30])
class AppSchedulerTest {
    
    private lateinit var context: Context
    private lateinit var appScheduler: AppScheduler
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appScheduler = AppScheduler(context)
    }
    
    @Test
    fun testGenerateRequestCode() {
        val requestCode1 = appScheduler.generateRequestCode()
        val requestCode2 = appScheduler.generateRequestCode()
        
        assertTrue(requestCode1 >= 1000)
        assertTrue(requestCode2 >= 1000)
        assertTrue(requestCode1 != requestCode2) // Should be different (very high probability)
    }
    
    @Test
    fun testCanScheduleExactAlarms() {
        // This will return true in Robolectric for API < 31
        val canSchedule = appScheduler.canScheduleExactAlarms()
        assertTrue(canSchedule)
    }
    
    @Test
    fun testScheduleAlarm() {
        val schedule = Schedule(
            id = 1L,
            packageName = "com.example.test",
            appName = "Test App",
            triggerAtMillis = System.currentTimeMillis() + 60000,
            requestCode = 12345,
            status = ScheduleStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val result = appScheduler.scheduleAlarm(schedule)
        assertTrue(result)
    }
    
    @Test
    fun testCancelAlarm() {
        val schedule = Schedule(
            id = 1L,
            packageName = "com.example.test",
            appName = "Test App",
            triggerAtMillis = System.currentTimeMillis() + 60000,
            requestCode = 12345,
            status = ScheduleStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Schedule first
        val scheduleResult = appScheduler.scheduleAlarm(schedule)
        assertTrue(scheduleResult)
        
        // Then cancel
        val cancelResult = appScheduler.cancelAlarm(schedule)
        assertTrue(cancelResult)
    }
}