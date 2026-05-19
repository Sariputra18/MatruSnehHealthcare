package com.matrusneh.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kick_events")
data class KickEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val date: String // "yyyy-MM-dd"
)
