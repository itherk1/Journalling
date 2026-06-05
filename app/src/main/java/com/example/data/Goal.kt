package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val timeframe: String, // WEEKLY, MONTHLY, QUARTERLY, HALFYEARLY, YEARLY
    val isAchieved: Boolean = false,
    val targetDate: Long = 0L
)
