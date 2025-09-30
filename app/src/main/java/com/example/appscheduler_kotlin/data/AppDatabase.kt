package com.example.appscheduler_kotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Schedule::class], version = 2, exportSchema = false) // Version incremented
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 1 to 2: Add appLabel column
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE schedules ADD COLUMN appLabel TEXT NOT NULL DEFAULT ''")
            }
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_scheduler_database"
                )
                .addMigrations(MIGRATION_1_2) // Added migration
                .build()
                INSTANCE = instance
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
