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
    val photoUri: String? = null, // local string URI representing the photo (kept for compatibility)
    val photoUris: List<String> = emptyList(), // added for multiple photos
    val location: String? = null,
    val linkedEntryIds: List<Int> = emptyList(),
    val isPrompt: Boolean = false,
    val backgroundColor: String? = null,
    val fontFamily: String? = null
)
