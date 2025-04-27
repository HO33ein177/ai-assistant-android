package com.example.bio.presentation.common.component.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.MessageDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationListViewModel @Inject constructor(
    private val messageDao: MessageDao
) : ViewModel() {

    private val _conversationIds = MutableStateFlow<List<String>>(emptyList())
    val conversationIds: StateFlow<List<String>> = _conversationIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: Int? = null

    fun loadConversations(userId: Int) {
        // Avoid reloading if already loaded for the same user
        if (userId == currentUserId && _conversationIds.value.isNotEmpty()) {
            return
        }
        currentUserId = userId
        _isLoading.value = true

        viewModelScope.launch {
            messageDao.getAllConversationIds(userId)
                .catch { e ->
                    Log.e("ConvListViewModel", "Error loading conversation IDs", e)
                    // Optionally expose an error state to the UI
                    _isLoading.value = false
                }
                .collect { ids ->
                    _conversationIds.value = ids
                    _isLoading.value = false
                    Log.d("ConvListViewModel", "Loaded ${ids.size} conversation IDs")
                }
        }
    }
}