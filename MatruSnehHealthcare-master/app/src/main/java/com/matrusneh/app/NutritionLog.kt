package com.matrusneh.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_log")
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val food: String,
    val isChecked: Boolean
)
