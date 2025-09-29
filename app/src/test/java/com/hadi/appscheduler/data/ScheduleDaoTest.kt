package com.hadi.appscheduler.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class ScheduleDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var scheduleDao: ScheduleDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        scheduleDao = database.scheduleDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetSchedule() = runBlocking {
        val schedule = Schedule(
            packageName = "com.example.test",
            appName = "Test App",
            triggerAtMillis = System.currentTimeMillis() + 60000,
            requestCode = 12345,
            status = ScheduleStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val scheduleId = scheduleDao.insertSchedule(schedule)
        val retrievedSchedule = scheduleDao.getScheduleById(scheduleId)
        
        assertNotNull(retrievedSchedule)
        assertEquals(schedule.packageName, retrievedSchedule.packageName)
        assertEquals(schedule.appName, retrievedSchedule.appName)
        assertEquals(schedule.status, retrievedSchedule.status)
    }
    
    @Test
    fun updateScheduleStatus() = runBlocking {
        val schedule = Schedule(
            packageName = "com.example.test",
            appName = "Test App",
            triggerAtMillis = System.currentTimeMillis() + 60000,
            requestCode = 12345,
            status = ScheduleStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val scheduleId = scheduleDao.insertSchedule(schedule)
        
        val updateTime = System.currentTimeMillis()
        scheduleDao.updateScheduleStatus(
            scheduleId, 
            ScheduleStatus.FIRED, 
            updateTime, 
            "Alarm fired successfully"
        )
        
        val updatedSchedule = scheduleDao.getScheduleById(scheduleId)
        
        assertNotNull(updatedSchedule)
        assertEquals(ScheduleStatus.FIRED, updatedSchedule.status)
        assertEquals("Alarm fired successfully", updatedSchedule.resultMessage)
        assertEquals(updateTime, updatedSchedule.updatedAt)
    }
    
    @Test
    fun getUpcomingSchedules() = runBlocking {
        val currentTime = System.currentTimeMillis()
        
        // Insert past schedule
        val pastSchedule = Schedule(
            packageName = "com.example.past",
            appName = "Past App",
            triggerAtMillis = currentTime - 60000,
            requestCode = 11111,
            status = ScheduleStatus.SCHEDULED,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        // Insert future schedule
        val futureSchedule = Schedule(
            packageName = "com.example.future",
            appName = "Future App",
            triggerAtMillis = currentTime + 60000,
            requestCode = 22222,
            status = ScheduleStatus.SCHEDULED,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        scheduleDao.insertSchedule(pastSchedule)
        scheduleDao.insertSchedule(futureSchedule)
        
        val upcomingSchedules = scheduleDao.getUpcomingSchedules(currentTime)
        
        assertEquals(1, upcomingSchedules.size)
        assertEquals("Future App", upcomingSchedules[0].appName)
    }
    
    @Test
    fun getScheduleCountAtTime() = runBlocking {
        val triggerTime = System.currentTimeMillis() + 60000
        val currentTime = System.currentTimeMillis()
        
        val schedule1 = Schedule(
            packageName = "com.example.test1",
            appName = "Test App 1",
            triggerAtMillis = triggerTime,
            requestCode = 11111,
            status = ScheduleStatus.SCHEDULED,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        val schedule2 = Schedule(
            packageName = "com.example.test2",
            appName = "Test App 2",
            triggerAtMillis = triggerTime,
            requestCode = 22222,
            status = ScheduleStatus.SCHEDULED,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        scheduleDao.insertSchedule(schedule1)
        scheduleDao.insertSchedule(schedule2)
        
        val count = scheduleDao.getScheduleCountAtTime(triggerTime)
        assertEquals(2, count)
    }
    
    @Test
    fun deleteSchedule() = runBlocking {
        val schedule = Schedule(
            packageName = "com.example.test",
            appName = "Test App",
            triggerAtMillis = System.currentTimeMillis() + 60000,
            requestCode = 12345,
            status = ScheduleStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val scheduleId = scheduleDao.insertSchedule(schedule)
        var retrievedSchedule = scheduleDao.getScheduleById(scheduleId)
        assertNotNull(retrievedSchedule)
        
        scheduleDao.deleteSchedule(retrievedSchedule)
        retrievedSchedule = scheduleDao.getScheduleById(scheduleId)
        assertNull(retrievedSchedule)
    }
}