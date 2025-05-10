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

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val messageDao: MessageDao
) : ViewModel() {

    // Changed to hold ConversationSummary objects
    private val _conversationSummaries = MutableStateFlow<List<ConversationSummary>>(emptyList())
    val conversationSummaries: StateFlow<List<ConversationSummary>> = _conversationSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: Int? = null

    // Fetches summaries of conversations (ID, first message, last message timestamp)
    fun loadConversationSummaries(userId: Int) {
        if (userId == currentUserId && _conversationSummaries.value.isNotEmpty() && !_isLoading.value) {
            Log.d("ConvListViewModel", "Summaries already loaded for user $userId")
            return
        }
        if (userId == currentUserId && _isLoading.value) {
            Log.d("ConvListViewModel", "Summaries already loading for user $userId")
            return
        }

        Log.d("ConvListViewModel", "Loading conversation summaries for user $userId")
        currentUserId = userId
        _isLoading.value = true
        _conversationSummaries.value = emptyList()

        viewModelScope.launch {
            messageDao.getConversationSummaries(userId) // Call the new DAO method
                .catch { e ->
                    Log.e("ConvListViewModel", "Error loading conversation summaries for user $userId", e)
                    _isLoading.value = false
                }
                .collect { summaries ->
                    _conversationSummaries.value = summaries.sortedByDescending { it.lastMessageTimestamp } // Sort by most recent
                    _isLoading.value = false
                    Log.d("ConvListViewModel", "Loaded ${summaries.size} conversation summaries for user $userId")
                }
        }
    }
}
