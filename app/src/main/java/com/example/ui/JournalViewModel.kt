package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.fetchDailyPrompts
import com.example.data.JournalEntry
import com.example.data.JournalRepository
import com.example.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _focusArea = MutableStateFlow(prefs.getFocusArea())
    val focusArea: StateFlow<String> = _focusArea.asStateFlow()

    private val _userGoals = MutableStateFlow(prefs.getGoals())
    val userGoals: StateFlow<String> = _userGoals.asStateFlow()

    private val _isFirstTimeOpen = MutableStateFlow(prefs.isFirstTimeOpen())
    val isFirstTimeOpen: StateFlow<Boolean> = _isFirstTimeOpen.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.isAppLockEnabled())
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    fun updateProfile(newName: String, newAge: String, newGender: String, newFocusArea: String = focusArea.value, newGoals: String = userGoals.value) {
        prefs.setName(newName)
        prefs.setAge(newAge)
        prefs.setGender(newGender)
        prefs.setFocusArea(newFocusArea)
        prefs.setGoals(newGoals)
        _name.value = newName
        _age.value = newAge
        _gender.value = newGender
        _focusArea.value = newFocusArea
        _userGoals.value = newGoals
    }

    fun completeOnboarding(newName: String, newAge: String, newGender: String, newFocusArea: String, newGoals: String) {
        updateProfile(newName, newAge, newGender, newFocusArea, newGoals)
        prefs.setFirstTimeOpen(false)
        _isFirstTimeOpen.value = false
        refreshPrompt()
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.setAppLockEnabled(enabled)
        _appLockEnabled.value = enabled
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedMoodFilter: StateFlow<String?> = _selectedMoodFilter.asStateFlow()

    private val _dailyPrompts = MutableStateFlow<List<String>>(listOf("Loading prompts..."))
    val dailyPrompts: StateFlow<List<String>> = _dailyPrompts.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        loadPrompt()
    }

    private fun loadPrompt() {
        viewModelScope.launch {
            _dailyPrompts.value = fetchDailyPrompts(_focusArea.value, _userGoals.value)
        }
    }

    fun removePrompt(prompt: String) {
        _dailyPrompts.value = _dailyPrompts.value.filter { it != prompt }
        if (_dailyPrompts.value.isEmpty()) {
            loadPrompt()
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

    fun addEntry(title: String, content: String, mood: String, photoUris: List<String>?, location: String?, isPrompt: Boolean = false, backgroundColor: String? = null, fontFamily: String? = null, linkedEntryIds: List<Int> = emptyList()) {
        viewModelScope.launch {
            repository.insert(
                JournalEntry(
                    title = title,
                    content = content,
                    mood = mood,
                    photoUris = photoUris ?: emptyList(),
                    location = location,
                    isPrompt = isPrompt,
                    backgroundColor = backgroundColor,
                    fontFamily = fontFamily,
                    linkedEntryIds = linkedEntryIds
                )
            )
            if (isPrompt) {
                removePrompt(title)
            }
        }
    }
    
    fun updateEntry(id: Int, title: String, content: String, timestamp: Long, mood: String, photoUris: List<String>?, location: String?, backgroundColor: String? = null, fontFamily: String? = null, linkedEntryIds: List<Int> = emptyList()) {
        viewModelScope.launch {
            repository.insert(
                JournalEntry(
                    id = id,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    mood = mood,
                    photoUris = photoUris ?: emptyList(),
                    location = location,
                    backgroundColor = backgroundColor,
                    fontFamily = fontFamily,
                    linkedEntryIds = linkedEntryIds
                )
            )
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
    val goals = repository.allGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val visionItems = repository.allVisionItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addGoal(title: String, description: String, timeframe: String, targetDate: Long) {
        viewModelScope.launch {
            repository.insertGoal(com.example.data.Goal(title = title, description = description, timeframe = timeframe, targetDate = targetDate))
        }
    }

    fun updateGoalStatus(id: Int, isAchieved: Boolean) {
        viewModelScope.launch {
            repository.updateGoalStatus(id, isAchieved)
        }
    }

    fun addVisionItem(type: String, content: String) {
        viewModelScope.launch {
            repository.insertVisionItem(com.example.data.VisionItem(type = type, content = content))
        }
    }

    fun deleteVisionItem(id: Int) {
        viewModelScope.launch {
            repository.deleteVisionItem(id)
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
