package com.example.bio.presentation.common.component.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.UserPreferenceDao
import com.example.bio.data.local.entity.UserPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val userPreferenceDao: UserPreferenceDao) : ViewModel() {


    fun saveUserPreferences(userId: Int, theme: String, language: String, voiceEnabled: Boolean) {
        viewModelScope.launch {
            val userPreferences = UserPreferences(userId = userId, theme = theme, language = language, voiceEnabled = voiceEnabled)
            userPreferenceDao.insert(userPreferences)
        }
    }

    fun getUserPreferences(userId: Int, onSuccess: (UserPreferences?) -> Unit) {
        viewModelScope.launch {
            val userPreferences = userPreferenceDao.getUserPreferences(userId)
            onSuccess(userPreferences)
        }
    }
}