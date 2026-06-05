package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vision_items")
data class VisionItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // IMAGE, TEXT
    val content: String // URI or actual text
)
