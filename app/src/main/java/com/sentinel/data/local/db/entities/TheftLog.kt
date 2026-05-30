package com.sentinel.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theft_logs")
data class TheftLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val triggerType: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val audioPath: String?,
    val isSynced: Boolean = false
)
