package com.example.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    fun searchEntries(query: String): Flow<List<JournalEntry>> {
        return journalDao.searchEntries(query)
    }
    
    suspend fun getEntryById(id: Int): JournalEntry? {
        return journalDao.getEntryById(id)
    }

    suspend fun insert(entry: JournalEntry): Long {
        return journalDao.insertEntry(entry)
    }

    suspend fun deleteById(id: Int) {
        journalDao.deleteEntryById(id)
    }
}
