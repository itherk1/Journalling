package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)
}
