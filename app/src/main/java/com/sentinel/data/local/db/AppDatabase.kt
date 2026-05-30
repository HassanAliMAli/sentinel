package com.sentinel.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sentinel.data.local.db.dao.AppConfigDao
import com.sentinel.data.local.db.dao.TheftLogDao
import com.sentinel.data.local.db.entities.AppConfig
import com.sentinel.data.local.db.entities.TheftLog

@Database(entities = [TheftLog::class, AppConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun theftLogDao(): TheftLogDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        const val DATABASE_NAME = "sentinel_db"
    }
}
