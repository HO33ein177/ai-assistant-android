package com.example.bio.presentation.common.component.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.MessageDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // <-- Add this annotation
class ConversationListViewModel @Inject constructor( // <-- Add @Inject here
    private val messageDao: MessageDao // Dependency is already a constructor param
) : ViewModel() {

    private val _conversationIds = MutableStateFlow<List<String>>(emptyList())
    val conversationIds: StateFlow<List<String>> = _conversationIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: Int? = null

    fun loadConversations(userId: Int) {
        // Avoid reloading if already loaded for the same user
        if (userId == currentUserId && _conversationIds.value.isNotEmpty() && !_isLoading.value) {
            Log.d("ConvListViewModel", "Conversations already loaded for user $userId")
            return
        }
        // Prevent multiple concurrent loads for the same user
        if (userId == currentUserId && _isLoading.value) {
            Log.d("ConvListViewModel", "Conversations already loading for user $userId")
            return
        }

        Log.d("ConvListViewModel", "Loading conversations for user $userId")
        currentUserId = userId
        _isLoading.value = true
        // Clear previous results when loading for a new user or reloading
        _conversationIds.value = emptyList()


        viewModelScope.launch {
            messageDao.getAllConversationIds(userId)
                .catch { e ->
                    Log.e("ConvListViewModel", "Error loading conversation IDs for user $userId", e)
                    // Optionally expose an error state to the UI
                    _isLoading.value = false // Ensure loading stops on error
                }
                .collect { ids ->
                    _conversationIds.value = ids
                    _isLoading.value = false
                    Log.d("ConvListViewModel", "Loaded ${ids.size} conversation IDs for user $userId")
                }
        }
    }
}
