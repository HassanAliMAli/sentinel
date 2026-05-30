package com.sentinel.data.local.db.dao

import androidx.room.*
import com.sentinel.data.local.db.entities.AppConfig

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE `key` = :key")
    suspend fun getConfig(key: String): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfig)
}
