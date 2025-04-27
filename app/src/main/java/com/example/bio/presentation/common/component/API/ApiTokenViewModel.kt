package com.example.bio.presentation.common.component.API

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.entity.ApiToken
import com.example.bio.data.local.dao.ApiTokenDao
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiTokenViewModel @Inject constructor(private val apiTokenDao: ApiTokenDao): ViewModel() {


    fun saveApiToken(userId: Int, geminiApiKey: String) {
        viewModelScope.launch {
            val apiToken = ApiToken(userId = userId, geminiApiKey = geminiApiKey)
            apiTokenDao.insert(apiToken)
        }
    }

    fun getApiToken(userId: Int, onSuccess: (ApiToken?) -> Unit) {
        viewModelScope.launch {
            val apiToken = apiTokenDao.getApiToken(userId)
            onSuccess(apiToken)
        }
    }
}