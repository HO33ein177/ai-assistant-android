package com.example.bio.presentation.common.component.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.AudioRecorderManager
import com.example.bio.data.local.dao.ApiTokenDao
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

// Use your package name

// Import your data layer classes

// Define constants for sender types
const val SENDER_USER = "user"
const val SENDER_AI = "ai"

// Update constructor to accept DAOs
@HiltViewModel
class ChatViewModel @Inject constructor(

    private val messageDao: MessageDao, // Inject MessageDao
    private val apiTokenDao: ApiTokenDao,   // Inject ApiTokenDao
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {

    // Keep existing state flows
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Add state for API key and GenerativeModel (initialized later)
    private val _apiKey = MutableStateFlow<String?>(null)
    private var generativeModel: GenerativeModel? = null // Make nullable

    // Store current user and conversation IDs
    private var currentUserId: Int? = null
    private var currentConversationId: String? = null
    private var historyLoadingJob: Job? = null

    // Keep AudioRecorderManager
    private val audioRecorder = AudioRecorderManager(applicationContext)

    // No init block needed anymore, initialization happens in loadDataForConversation

    // Function to load data for a specific user and conversation
    fun loadDataForConversation(userId: Int, conversationId: String) {
        if (currentUserId == userId && currentConversationId == conversationId) {
            Log.d(
                "ChatViewModel",
                "Data already loaded for user $userId, conversation $conversationId"
            )
            return // Avoid reloading if already loaded
        }

        Log.d("ChatViewModel", "Loading data for user $userId, conversation $conversationId")
        currentUserId = userId
        currentConversationId = conversationId
        _chatHistory.value = emptyList() // Clear previous history
        _isLoading.value = true // Show loading indicator

        // Cancel any previous loading job
        historyLoadingJob?.cancel()

        viewModelScope.launch {
            // 1. Fetch API Key
            val token = fetchApiKey(userId)
            if (token == null) {
                handleError("API Key not found for user $userId. Please set it in settings.", null)
                _isLoading.value = false
                return@launch // Stop loading if no key
            }
            _apiKey.value = token

            // 2. Initialize Generative Model (only if key is valid)
            initializeGenerativeModel(token)
            if (generativeModel == null) {
                handleError("Failed to initialize AI Model with the provided key.", null)
                _isLoading.value = false
                return@launch // Stop if model init fails
            }

            // 3. Load Chat History
            loadAndObserveHistory(userId, conversationId)
            // Loading state will be set to false inside loadAndObserveHistory's catch/finally
        }
    }

    // Helper to fetch API Key
    private suspend fun fetchApiKey(userId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                apiTokenDao.getApiToken(userId)?.geminiApiKey
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching API key for user $userId", e)
                null
            }
        }
    }

    // Helper to initialize GenerativeModel
    private fun initializeGenerativeModel(apiKey: String) {
        try {
            val config = generationConfig { /* Add your specific config if needed */ }
            generativeModel = GenerativeModel(
                // Use a model known to support your needs (e.g., text, audio)
                modelName = "gemini-1.5-flash",
                apiKey = apiKey, // Use the fetched key
                generationConfig = config
            )
            Log.d("ChatViewModel", "GenerativeModel initialized successfully.")
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to initialize GenerativeModel", e)
            generativeModel = null // Ensure it's null on failure
        }
    }


    // Helper to load and observe history from DB
    private fun loadAndObserveHistory(userId: Int, conversationId: String) {
        historyLoadingJob = viewModelScope.launch {
            messageDao.getMessagesForConversation(userId, conversationId)
                .catch { throwable -> // Explicitly name the exception throwable
                    // Pass the caught throwable to handleError
                    handleError("Error loading chat history: ${throwable.localizedMessage}", throwable as? Exception ?: RuntimeException(throwable)) // Cast or wrap if needed
                    _isLoading.value = false // Stop loading on error
                }
                .collect { dbMessages ->
                    Log.d("ChatViewModel", "Loaded ${dbMessages.size} messages from DB.")
                    // Map database Message objects to UI ChatMessage objects
                    val uiMessages = dbMessages.map { dbMsg ->
                        ChatMessage(
                            text = dbMsg.content, // Use the correct content field
                            isFromUser = dbMsg.sender == SENDER_USER,
                            isError = false, // Assume DB messages are not errors initially
                            // Determine message type based on audio path or other logic if needed
                            messageType = if (dbMsg.audioPath != null) MessageType.AUDIO else MessageType.TEXT,
                            audioFilePath = dbMsg.audioPath
                        )
                    }
                    _chatHistory.value = uiMessages
                    _isLoading.value = false // Stop loading once history is processed
                    Log.d("ChatViewModel", "Chat history updated in UI.")
                }
        }
    }


    // Modified sendMessage
    fun sendMessage(userInput: String) {
        val userId = currentUserId ?: return // Need userId
        val conversationId = currentConversationId ?: return // Need conversationId
        val model = generativeModel ?: run { // Check if model is initialized
            handleError("AI Model not ready.", null)
            return
        }


        if (userInput.isBlank() || _isLoading.value || _isRecording.value) return

        _isLoading.value = true

        // Create UI message
        val userChatMessage = ChatMessage(
            text = userInput,
            isFromUser = true,
            messageType = MessageType.TEXT
        )
        // Update UI immediately
        _chatHistory.update { it + userChatMessage }

        // Create Database message
        val userDbMessage = Message(
            userId = userId,
            conversationId = conversationId,
            sender = SENDER_USER,
            content = userInput,
            timestamp = System.currentTimeMillis() // Explicitly set timestamp if needed elsewhere
        )

        viewModelScope.launch {
            // Save user message to DB (background)
            withContext(Dispatchers.IO) {
                try {
                    messageDao.insert(userDbMessage)
                    Log.d("ChatViewModel", "User message saved to DB.")
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Failed to save user message to DB", e)
                    // Optionally: Update the UI message to show a save error
                }
            }

            // Call API
            try {
                val response = model.generateContent(userInput)
                val responseText = response.text ?: "Sorry, I couldn't generate a text response."

                // Create UI message for AI response
                val aiChatMessage = ChatMessage(
                    text = responseText,
                    isFromUser = false,
                    messageType = MessageType.TEXT
                )
                // Update UI
                _chatHistory.update { it + aiChatMessage }


                // Create Database message for AI response
                val aiDbMessage = Message(
                    userId = userId,
                    conversationId = conversationId,
                    sender = SENDER_AI,
                    content = responseText,
                    timestamp = System.currentTimeMillis()
                )
                // Save AI message to DB (background)
                withContext(Dispatchers.IO) {
                    try {
                        messageDao.insert(aiDbMessage)
                        Log.d("ChatViewModel", "AI message saved to DB.")
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Failed to save AI message to DB", e)
                    }
                }
            } catch (e: Exception) {
                handleError("API Error (Text): ${e.localizedMessage}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Audio Recording Functions ---

    fun startRecordingAudio() {
        if (_isLoading.value || _isRecording.value) return
        viewModelScope.launch {
            val success = audioRecorder.startRecording() != null
            if (success) {
                _isRecording.value = true
                Log.i("ChatViewModel", "Recording Started")
            } else {
                handleError("Error: Could not start recording.", null)
                Log.e("ChatViewModel", "Failed to start recording")
            }
        }
    }

    // Modified stopRecordingAudioAndSend
    fun stopRecordingAudioAndSend(prompt: String = "Describe this audio") {
        val userId = currentUserId ?: return // Need userId
        val conversationId = currentConversationId ?: return // Need conversationId
        val model = generativeModel ?: run { // Check if model is initialized
            handleError("AI Model not ready.", null)
            return
        }

        if (!_isRecording.value) return

        viewModelScope.launch {
            _isRecording.value = false
            _isLoading.value = true

            val tempAudioFilePath = audioRecorder.stopRecording()

            if (tempAudioFilePath != null) {
                Log.i("ChatViewModel", "Recording Stopped, temp file: $tempAudioFilePath.")
                val tempAudioFile = File(tempAudioFilePath)
                var persistentAudioFilePath: String? = null

                if (tempAudioFile.exists() && tempAudioFile.length() > 0) {
                    // --- Copy to Persistent Storage ---
                    persistentAudioFilePath = copyAudioToInternalStorage(tempAudioFile)

                    if (persistentAudioFilePath != null) {
                        // --- Add AUDIO message to UI ---
                        val audioChatMessage = ChatMessage(
                            text = prompt, // Store the prompt
                            isFromUser = true,
                            messageType = MessageType.AUDIO,
                            audioFilePath = persistentAudioFilePath // Pass persistent path
                        )
                        _chatHistory.update { it + audioChatMessage }
                        Log.d(
                            "ChatViewModel",
                            "Added audio message to UI. Path: $persistentAudioFilePath"
                        )

                        // --- Save AUDIO message placeholder to DB ---
                        val audioDbMessage = Message(
                            userId = userId,
                            conversationId = conversationId,
                            sender = SENDER_USER,
                            content = prompt, // Store the prompt in content
                            timestamp = System.currentTimeMillis(),
                            audioPath = persistentAudioFilePath // Store the path in DB
                        )
                        withContext(Dispatchers.IO) {
                            try {
                                messageDao.insert(audioDbMessage)
                                Log.d("ChatViewModel", "Audio message placeholder saved to DB.")
                            } catch (e: Exception) {
                                Log.e(
                                    "ChatViewModel",
                                    "Failed to save audio message placeholder to DB",
                                    e
                                )
                            }
                        }


                        // --- Send to API ---
                        try {
                            val audioBytes = tempAudioFile.readBytes()
                            val inputContent = content {
                                text(prompt)
                                part(BlobPart(mimeType = "audio/m4a", blob = audioBytes))
                            }

                            Log.d("ChatViewModel", "Sending content with audio to Gemini...")
                            val response = model.generateContent(inputContent) // Use initialized model
                            Log.d("ChatViewModel", "Received response from Gemini.")
                            val responseText = response.text ?: "Sorry, I couldn't process the audio."

                            // --- Add API response (TEXT) to UI ---
                            val aiTextChatMessage = ChatMessage(
                                text = responseText,
                                isFromUser = false,
                                messageType = MessageType.TEXT
                            )
                            _chatHistory.update { it + aiTextChatMessage }


                            // --- Save API response (TEXT) to DB ---
                            val aiTextDbMessage = Message(
                                userId = userId,
                                conversationId = conversationId,
                                sender = SENDER_AI,
                                content = responseText,
                                timestamp = System.currentTimeMillis()
                            )
                            withContext(Dispatchers.IO) {
                                try {
                                    messageDao.insert(aiTextDbMessage)
                                    Log.d("ChatViewModel", "AI response (from audio) saved to DB.")
                                } catch (e: Exception) {
                                    Log.e(
                                        "ChatViewModel",
                                        "Failed to save AI response (from audio) to DB",
                                        e
                                    )
                                }
                            }

                        } catch (e: IOException) {
                            handleError("Error reading audio file bytes: ${e.localizedMessage}", e)
                        } catch (e: Exception) {
                            handleError("API Error (Audio): ${e.localizedMessage}", e)
                        } finally {
                            // Clean up TEMPORARY audio file
                            try {
                                if (tempAudioFile.delete()) {
                                    Log.d(
                                        "ChatViewModel",
                                        "Deleted temp audio file: $tempAudioFilePath"
                                    )
                                } else {
                                    Log.w(
                                        "ChatViewModel",
                                        "Failed to delete temp audio file: $tempAudioFilePath"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "ChatViewModel",
                                    "Error deleting temp audio file: $tempAudioFilePath",
                                    e
                                )
                            }
                        } // End API try/catch/finally

                    } else { // Copying failed
                        handleError("Error: Failed to copy audio to persistent storage.", null)
                        tempAudioFile.delete()
                    } // End check persistentAudioFilePath != null
                } else { // Temp file missing/empty
                    handleError("Error: Recorded audio file is missing or empty.", null)
                    tempAudioFile.delete()
                } // End check tempAudioFile exists
            } else { // stopRecording failed
                handleError("Error: Failed to stop recording or get file path.", null)
            } // End check tempAudioFilePath != null

            _isLoading.value = false // Ensure loading stops
        } // End viewModelScope.launch
    }

    // --- Helper function to copy audio (remains the same) ---
    private suspend fun copyAudioToInternalStorage(sourceFile: File): String? {
        // ... (implementation is the same as before) ...
        return withContext(Dispatchers.IO) { // Perform file IO on background thread
            try {
                val internalFilesDir = applicationContext.filesDir // App's private internal storage
                if (!internalFilesDir.exists()) {
                    internalFilesDir.mkdirs()
                }
                // Create a unique filename
                val destinationFileName = "audio_${UUID.randomUUID()}.m4a"
                val destinationFile = File(internalFilesDir, destinationFileName)

                sourceFile.copyTo(destinationFile, overwrite = true)
                Log.d("ChatViewModel", "Audio copied to: ${destinationFile.absolutePath}")
                destinationFile.absolutePath // Return the new path
            } catch (e: IOException) {
                Log.e("ChatViewModel", "Failed to copy audio file: ${e.message}", e)
                null // Return null on failure
            }
        }
    }

    // --- Modified Helper to add ERROR messages TO UI ONLY ---
    // DB saving happens where the error occurs if needed
    private fun handleError(message: String, exception: Exception?) {
        Log.e("ChatViewModel", message, exception)
        // Create an error ChatMessage for the UI
        val errorChatMessage = ChatMessage(
            text = "Error: ${
                message.substringBefore('\n').substringBefore(':')
            }", // Keep UI error concise
            isFromUser = false, // Errors are typically shown as system/AI messages
            isError = true,
            messageType = MessageType.TEXT // Display errors as text
        )
        // Update UI StateFlow
        _chatHistory.update { it + errorChatMessage }
        // Optionally, you might want to save this error to the DB as well
        // Depends on whether you want errors persisted in history
    }


    override fun onCleared() {
        super.onCleared()
        audioRecorder.releaseRecorder()
        historyLoadingJob?.cancel() // Cancel loading job if ViewModel is cleared
        Log.d("ChatViewModel", "ViewModel cleared, recorder released, jobs cancelled.")
    }

    // Remove the old factory companion object if it exists
    // companion object { ... } // DELETE THIS
}