package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "journal_entries")
@Serializable
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String = "NEUTRAL", // HAPPY, SAD, NEUTRAL, EXCITED, ANXIOUS, etc.
    val photoUri: String? = null // local string URI representing the photo
)
