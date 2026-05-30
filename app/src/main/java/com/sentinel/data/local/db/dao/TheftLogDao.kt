package com.sentinel.data.local.db.dao

import androidx.room.*
import com.sentinel.data.local.db.entities.TheftLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TheftLogDao {
    @Query("SELECT * FROM theft_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TheftLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TheftLog): Long

    @Delete
    suspend fun deleteLog(log: TheftLog)

    @Query("SELECT * FROM theft_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<TheftLog>

    @Update
    suspend fun updateLog(log: TheftLog)
}
