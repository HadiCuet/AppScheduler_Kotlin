package com.example.appscheduler_kotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Schedule::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // We'll add this later if needed for complex types like Date or Enum persistence
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_scheduler_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this initial build.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

// Basic TypeConverter for ScheduleStatus.
// Room needs this to know how to store and retrieve the enum.
class Converters {
    @androidx.room.TypeConverter
    fun fromScheduleStatus(value: ScheduleStatus?): String? {
        return value?.name
    }

    @androidx.room.TypeConverter
    fun toScheduleStatus(value: String?): ScheduleStatus? {
        return value?.let { ScheduleStatus.valueOf(it) }
    }
}
