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

    val allGoals: Flow<List<Goal>> = journalDao.getAllGoals()
    
    suspend fun insertGoal(goal: Goal) {
        journalDao.insertGoal(goal)
    }

    suspend fun updateGoalStatus(id: Int, isAchieved: Boolean) {
        journalDao.updateGoalStatus(id, isAchieved)
    }

    suspend fun deleteGoal(id: Int) {
        journalDao.deleteGoal(id)
    }

    val allVisionItems: Flow<List<VisionItem>> = journalDao.getAllVisionItems()

    suspend fun insertVisionItem(item: VisionItem) {
        journalDao.insertVisionItem(item)
    }

    suspend fun deleteVisionItem(id: Int) {
        journalDao.deleteVisionItem(id)
    }
}
