package com.example.bio.presentation.common.component.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.AudioRecorderManager
import com.example.bio.data.local.dao.MessageDao
import com.example.bio.data.local.entity.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlobPart
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


// constants for sender types
const val SENDER_USER = "user"
const val SENDER_AI = "ai"
private const val TAG = "ChatViewModel"


private const val HARDCODED_API_KEY = "AIzaSyCIpO9iAbHgt3rTfBkHtX3NZDqBuJcI68I"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageDao: MessageDao,
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    open val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    open val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var generativeModel: GenerativeModel? = null

    private var currentUserId: Int? = null
    private var currentConversationId: String? = null
    private var historyLoadingJob: Job? = null

    private val audioRecorder = AudioRecorderManager(applicationContext)


    open fun loadDataForConversation(userId: Int, conversationId: String) {
        Log.d(TAG, "loadDataForConversation called for userId: $userId, conversationId: $conversationId")
        // Check if already initialized for this conversation
        if (currentConversationId == conversationId && generativeModel != null) {
            Log.d(TAG, "Model already initialized for this conversation. Skipping re-initialization.")
            loadAndObserveHistory(userId, conversationId)
            return
        }

        Log.d(TAG, "Starting data loading process...")
        currentUserId = userId // Still store current user ID if needed for DB
        currentConversationId = conversationId
        _chatHistory.value = emptyList()
        Log.d(TAG, "Setting isLoading = true")
        _isLoading.value = true

        historyLoadingJob?.cancel()
        Log.d(TAG, "Cancelled previous history loading job (if any).")

        viewModelScope.launch {
            Log.d(TAG, "Data loading coroutine started.")

            val token = HARDCODED_API_KEY
            if (token == "YOUR_API_KEY_HERE" || token.isBlank()) {
                Log.e(TAG, "*** API Key is not set in the HARDCODED_API_KEY constant! ***")
                handleError("API Key not configured in source code.", null)
                Log.d(TAG, "Setting isLoading = false (Hardcoded key missing)")
                _isLoading.value = false
                return@launch
            }
            Log.d(TAG, "Using hardcoded API Key.")



            Log.d(TAG, "Attempting to initialize Generative Model.")
            initializeGenerativeModel(token) // Use the token directly
            if (generativeModel == null) {
                Log.e(TAG, "Generative Model initialization failed.")
                handleError("Failed to initialize AI Model with the provided key.", null)
                Log.d(TAG, "Setting isLoading = false (Model init failed)")
                _isLoading.value = false
                return@launch
            }
            Log.d(TAG, "Generative Model initialized successfully.")

            Log.d(TAG, "Attempting to load and observe history.")
            // Pass the actual userId needed for the database query
            loadAndObserveHistory(userId, conversationId)
        }
    }


    //  initializeGenerativeModel
    private fun initializeGenerativeModel(apiKey: String) {
        try {
            val config = generationConfig {  }
            generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = apiKey,
                generationConfig = config
            )
            Log.i(TAG, "[initializeGenerativeModel] Success.")
        } catch (e: Exception) {
            Log.e(TAG, "[initializeGenerativeModel] Failed", e)
            generativeModel = null
        }
    }


    // loadAndObserveHistory
    private fun loadAndObserveHistory(userId: Int, conversationId: String) {
        Log.d(TAG, "loadAndObserveHistory started for userId: $userId, conversationId: $conversationId")
        historyLoadingJob = viewModelScope.launch {
            Log.d(TAG, "History loading coroutine launched.")
            messageDao.getMessagesForConversation(userId, conversationId) // Ensure userId is used here
                .catch { throwable ->
                    Log.e(TAG, "Error collecting chat history flow", throwable)
                    handleError("Error loading chat history: ${throwable.localizedMessage}", throwable as? Exception ?: RuntimeException(throwable))
                    Log.d(TAG, "Setting isLoading = false (History flow catch block)")
                    _isLoading.value = false
                }
                .collect { dbMessages ->
                    Log.d(TAG, "Collected ${dbMessages.size} messages from DB.")
                    val uiMessages = dbMessages.map { dbMsg ->
                        ChatMessage(
                            text = dbMsg.content,
                            isFromUser = dbMsg.sender == SENDER_USER,
                            isError = false,
                            messageType = if (dbMsg.audioPath != null) MessageType.AUDIO else MessageType.TEXT,
                            audioFilePath = dbMsg.audioPath
                        )
                    }
                    _chatHistory.value = uiMessages
                    Log.d(TAG, "Chat history updated in UI state.")
                    Log.d(TAG, "Setting isLoading = false (History flow collect block finished)")
                    _isLoading.value = false
                }
            Log.d(TAG, "History flow collection finished or job cancelled.")
            if (_isLoading.value) {
                Log.w(TAG, "History flow completed, but isLoading was still true. Setting to false.")
                _isLoading.value = false
            }
        }
        historyLoadingJob?.invokeOnCompletion { throwable ->
            if (throwable != null && throwable !is kotlinx.coroutines.CancellationException) {
                Log.e(TAG, "History loading job completed with error", throwable)
                if (_isLoading.value) {
                    Log.w(TAG, "Setting isLoading = false (History job completed with error)")
                    _isLoading.value = false
                }
            } else if (throwable is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "History loading job cancelled.")
            } else {
                Log.d(TAG, "History loading job completed successfully.")
                if (_isLoading.value) {
                    Log.w(TAG, "History job succeeded, but isLoading was still true. Setting to false.")
                    _isLoading.value = false
                }
            }
        }
    }

    // sendMessage, startRecordingAudio, stopRecordingAudioAndSend
    // Ensure use currentUserId when creating Message objects for DB insertion
    open fun sendMessage(userInput: String) {
        val userId = currentUserId ?: return run { Log.e(TAG, "[sendMessage] Failed: currentUserId is null") }
        val conversationId = currentConversationId ?: return run { Log.e(TAG, "[sendMessage] Failed: currentConversationId is null") }
        val model = generativeModel ?: return run {
            Log.e(TAG, "[sendMessage] Failed: generativeModel is null")
            handleError("AI Model not ready.", null)
        }

        if (userInput.isBlank()) return run { Log.w(TAG, "[sendMessage] Ignored: userInput is blank") }
        if (_isLoading.value) return run { Log.w(TAG, "[sendMessage] Ignored: isLoading is true") }
        if (_isRecording.value) return run { Log.w(TAG, "[sendMessage] Ignored: isRecording is true") }

        // rest of sendMessage
        // Ensure 'userId' used when creating userDbMessage and aiDbMessage
        val userDbMessage = Message(userId = userId, conversationId = conversationId, sender = SENDER_USER, content = userInput, timestamp = System.currentTimeMillis())
        // API call
        viewModelScope.launch {
            Log.d(TAG, "[sendMessage] Saving user message to DB...")
            withContext(Dispatchers.IO) {
                try {
                    messageDao.insert(userDbMessage) // Ensure userId used correctly
                    Log.d(TAG, "[sendMessage] User message saved to DB successfully.")
                } catch (e: Exception) {
                    Log.e(TAG, "[sendMessage] Failed to save user message to DB", e)
                }
            }

            Log.d(TAG, "[sendMessage] Calling Gemini API for text generation...")
            try {
                val response = model.generateContent(userInput)
                val responseText = response.text ?: "Sorry, I couldn't generate a text response."
                Log.d(TAG, "[sendMessage] Received Gemini response: '$responseText'")

                val aiChatMessage = ChatMessage(text = responseText, isFromUser = false, messageType = MessageType.TEXT)
                _chatHistory.update { it + aiChatMessage }
                Log.d(TAG, "[sendMessage] Updated UI with AI message.")

                val aiDbMessage = Message(userId = userId, conversationId = conversationId, sender = SENDER_AI, content = responseText, timestamp = System.currentTimeMillis()) // Ensure userId used correctly here
                Log.d(TAG, "[sendMessage] Saving AI message to DB...")
                withContext(Dispatchers.IO) {
                    try {
                        messageDao.insert(aiDbMessage)
                        Log.d(TAG, "[sendMessage] AI message saved to DB successfully.")
                    } catch (e: Exception) {
                        Log.e(TAG, "[sendMessage] Failed to save AI message to DB", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[sendMessage] API Error (Text)", e)
                handleError("API Error (Text): ${e.localizedMessage}", e)
            } finally {
                Log.d(TAG, "[sendMessage] Setting isLoading = false (finally block)")
                _isLoading.value = false
            }
        }
    }


    open fun startRecordingAudio() {
        if (_isLoading.value) return run { Log.w(TAG, "[startRecordingAudio] Ignored: isLoading is true") }
        if (_isRecording.value) return run { Log.w(TAG, "[startRecordingAudio] Ignored: already recording") }
        // ... rest of startRecordingAudio ...
        viewModelScope.launch {
            Log.d(TAG, "[startRecordingAudio] Attempting to start recording...")
            val success = audioRecorder.startRecording() != null
            if (success) {
                _isRecording.value = true
                Log.i(TAG, "[startRecordingAudio] Recording Started successfully.")
            } else {
                Log.e(TAG, "[startRecordingAudio] Failed to start recording.")
                handleError("Error: Could not start recording.", null)
            }
        }
    }


    open fun stopRecordingAudioAndSend(prompt: String = "Describe this audio") { // Keep prompt argument for potential future use or logging
        val userId = currentUserId ?: return run { Log.e(TAG, "[stopRecording] Failed: currentUserId is null") }
        val conversationId = currentConversationId ?: return run { Log.e(TAG, "[stopRecording] Failed: currentConversationId is null") }
        val model = generativeModel ?: return run {
            Log.e(TAG, "[stopRecording] Failed: generativeModel is null")
            handleError("AI Model not ready.", null)
        }
        if (!_isRecording.value) return run { Log.w(TAG, "[stopRecording] Ignored: not recording") }

        // Log the original prompt if needed
        Log.d(TAG, "[stopRecording] Stopping recording and processing. Original UI prompt/text was: '$prompt'")
        viewModelScope.launch {
            _isRecording.value = false
            _isLoading.value = true

            val tempAudioFilePath = audioRecorder.stopRecording()

            if (tempAudioFilePath != null) {
                val tempAudioFile = File(tempAudioFilePath)
                var persistentAudioFilePath: String? = null

                if (tempAudioFile.exists() && tempAudioFile.length() > 0) {
                    persistentAudioFilePath = copyAudioToInternalStorage(tempAudioFile)

                    if (persistentAudioFilePath != null) {
                        val voiceMessagePlaceholderText = "[Voice Message]"

                        //  AUDIO message with  placeholder text
                        val audioChatMessage = ChatMessage(
                            text = voiceMessagePlaceholderText,
                            isFromUser = true,
                            messageType = MessageType.AUDIO,
                            audioFilePath = persistentAudioFilePath
                        )
                        _chatHistory.update { it + audioChatMessage }
                        Log.d("ChatViewModel", "Added audio message placeholder to UI.")

                        // Save AUDIO message placeholder to DB with the placeholder text
                        val audioDbMessage = Message(
                            userId = userId,
                            conversationId = conversationId,
                            sender = SENDER_USER,
                            content = voiceMessagePlaceholderText, // Use placeholder
                            timestamp = System.currentTimeMillis(),
                            audioPath = persistentAudioFilePath
                        )

                        // save audioDbMessage to DB
                        withContext(Dispatchers.IO) { try { messageDao.insert(audioDbMessage); Log.d(TAG, "[stopRecording] Audio placeholder saved.") } catch (e: Exception) { Log.e(TAG, "[stopRecording] Failed to save audio placeholder", e) } }

                        // Send ONLY audio to API
                        try {
                            val audioBytes = tempAudioFile.readBytes()
                            val inputContent = content {
                                part(BlobPart(mimeType = "audio/m4a", blob = audioBytes))
                            }
                            Log.d("ChatViewModel", "Sending ONLY audio content to Gemini...")

                            val response = model.generateContent(inputContent)
                            Log.d("ChatViewModel", "Received response from Gemini.")
                            val responseText = response.text ?: "Sorry, I couldn't process the audio."

                            // Add AI response text to UI and DB
                            val aiTextChatMessage = ChatMessage(
                                text = responseText,
                                isFromUser = false,
                                messageType = MessageType.TEXT
                            )
                            _chatHistory.update { it + aiTextChatMessage }
                            val aiTextDbMessage = Message(
                                userId = userId,
                                conversationId = conversationId,
                                sender = SENDER_AI,
                                content = responseText,
                                timestamp = System.currentTimeMillis()
                            )
                            withContext(Dispatchers.IO) { try { messageDao.insert(aiTextDbMessage); Log.d(TAG, "[stopRecording] AI response saved.") } catch (e: Exception) { Log.e(TAG, "[stopRecording] Failed to save AI response", e) } }


                        } catch (e: IOException) {
                            handleError("Error reading audio file bytes: ${e.localizedMessage}", e)
                            Log.e(TAG, "[stopRecording] Error reading audio file bytes", e)
                        } catch (e: Exception) {
                            Log.e(TAG, "[stopRecording] API Error (Audio)", e)
                            handleError("API Error (Audio): ${e.localizedMessage}", e)
                        } finally {
                            // ... (Clean up temporary audio file) ...
                            try {
                                if (tempAudioFile.delete()) { Log.d(TAG, "[stopRecording] Temp file deleted.") }
                                else { Log.w(TAG, "[stopRecording] Failed to delete temp file.") }
                            } catch (e: Exception) { Log.e(TAG, "[stopRecording] Error deleting temp file", e) }
                        }
                    } else { // Copying failed
                        handleError("Error: Failed to copy audio to persistent storage.", null)
                        Log.e(TAG, "[stopRecording] Failed to copy audio to persistent storage.")
                        tempAudioFile.delete()
                    }
                } else { // Temp file missing/empty
                    handleError("Error: Recorded audio file is missing or empty.", null)
                    Log.e(TAG, "[stopRecording] Recorded audio file is missing or empty: $tempAudioFilePath")
                    if (tempAudioFile.exists()) tempAudioFile.delete()
                }
            } else { // stopRecording failed
                handleError("Error: Failed to stop recording or get file path.", null)
                Log.e(TAG, "[stopRecording] Failed to stop recording or get file path.")
            }

            _isLoading.value = false
        }
    }

    //  copyAudioToInternalStorage, handleError, onCleared
    private suspend fun copyAudioToInternalStorage(sourceFile: File): String? {
        Log.d(TAG, "[copyAudio] Copying ${sourceFile.absolutePath} to internal storage.")
        return withContext(Dispatchers.IO) {
            try {
                val internalFilesDir = applicationContext.filesDir
                if (!internalFilesDir.exists()) internalFilesDir.mkdirs()
                val destinationFileName = "audio_${UUID.randomUUID()}.m4a"
                val destinationFile = File(internalFilesDir, destinationFileName)
                sourceFile.copyTo(destinationFile, overwrite = true)
                Log.d(TAG, "[copyAudio] Copied successfully to: ${destinationFile.absolutePath}")
                destinationFile.absolutePath
            } catch (e: IOException) {
                Log.e(TAG, "[copyAudio] Failed to copy audio file", e)
                null
            }
        }
    }

    private fun handleError(message: String, exception: Exception?) {
        Log.e(TAG, "handleError called: $message", exception)
        val errorChatMessage = ChatMessage(
            text = "Error: ${message.substringBefore('\n').substringBefore(':')}",
            isFromUser = false,
            isError = true,
            messageType = MessageType.TEXT
        )
        _chatHistory.update { it + errorChatMessage }
        Log.d(TAG, "Added error message to UI chat history.")
        if (_isLoading.value) {
            Log.d(TAG, "Setting isLoading = false from handleError.")
            _isLoading.value = false
        }
        if (_isRecording.value) {
            Log.d(TAG, "Setting isRecording = false from handleError.")
            _isRecording.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared called. Releasing recorder and cancelling jobs.")
        audioRecorder.releaseRecorder()
        historyLoadingJob?.cancel()
    }

}