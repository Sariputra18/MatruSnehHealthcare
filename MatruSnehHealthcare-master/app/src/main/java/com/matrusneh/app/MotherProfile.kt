package com.matrusneh.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mother_profile")
data class MotherProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val edd: Long,
    val ashaPhone: String
)
