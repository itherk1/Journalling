package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.fetchDailyPrompt
import com.example.data.JournalEntry
import com.example.data.JournalRepository
import com.example.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: JournalRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _name = MutableStateFlow(prefs.getName())
    val name: StateFlow<String> = _name.asStateFlow()

    private val _age = MutableStateFlow(prefs.getAge())
    val age: StateFlow<String> = _age.asStateFlow()

    private val _gender = MutableStateFlow(prefs.getGender())
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.isAppLockEnabled())
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    fun updateProfile(newName: String, newAge: String, newGender: String) {
        prefs.setName(newName)
        prefs.setAge(newAge)
        prefs.setGender(newGender)
        _name.value = newName
        _age.value = newAge
        _gender.value = newGender
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.setAppLockEnabled(enabled)
        _appLockEnabled.value = enabled
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedMoodFilter: StateFlow<String?> = _selectedMoodFilter.asStateFlow()

    private val _dailyPrompt = MutableStateFlow("Loading prompt...")
    val dailyPrompt: StateFlow<String> = _dailyPrompt.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        loadPrompt()
    }

    private fun loadPrompt() {
        viewModelScope.launch {
            _dailyPrompt.value = fetchDailyPrompt()
        }
    }

    val entries = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun setMoodFilter(mood: String?) {
        _selectedMoodFilter.value = mood
    }

    fun setAuthenticated(auth: Boolean) {
        _isAuthenticated.value = auth
    }

    fun refreshPrompt() {
        loadPrompt()
    }

    fun addEntry(title: String, content: String, mood: String, photoUri: String?) {
        viewModelScope.launch {
            repository.insert(
                JournalEntry(
                    title = title,
                    content = content,
                    mood = mood,
                    photoUri = photoUri
                )
            )
        }
    }
    
    fun updateEntry(id: Int, title: String, content: String, timestamp: Long, mood: String, photoUri: String?) {
        viewModelScope.launch {
            repository.insert(
                JournalEntry(
                    id = id,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    mood = mood,
                    photoUri = photoUri
                )
            )
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

class JournalViewModelFactory(
    private val repository: JournalRepository,
    private val prefs: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
