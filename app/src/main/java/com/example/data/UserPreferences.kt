package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)

    fun getName() = prefs.getString("name", "") ?: ""
    fun setName(name: String) = prefs.edit().putString("name", name).apply()

    fun getAge() = prefs.getString("age", "") ?: ""
    fun setAge(age: String) = prefs.edit().putString("age", age).apply()

    fun getGender() = prefs.getString("gender", "") ?: ""
    fun setGender(gender: String) = prefs.edit().putString("gender", gender).apply()

    fun getFocusArea() = prefs.getString("focusArea", "") ?: ""
    fun setFocusArea(focusArea: String) = prefs.edit().putString("focusArea", focusArea).apply()

    fun getGoals() = prefs.getString("goals", "") ?: ""
    fun setGoals(goals: String) = prefs.edit().putString("goals", goals).apply()

    fun isFirstTimeOpen() = prefs.getBoolean("first_time_open", true)
    fun setFirstTimeOpen(isFirst: Boolean) = prefs.edit().putBoolean("first_time_open", isFirst).apply()

    fun isAppLockEnabled() = prefs.getBoolean("app_lock", true)
    fun setAppLockEnabled(enabled: Boolean) = prefs.edit().putBoolean("app_lock", enabled).apply()
}
