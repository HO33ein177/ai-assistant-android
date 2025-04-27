package com.example.bio.presentation.common.component.chat // Use your package name

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class) // Add ExperimentalPermissionsApi
@Composable
fun ChatScreen(
    // Add parameters to receive userId and conversationId
    // These should likely come from your Navigation setup
    userId: Int,
    conversationId: String
    // Note: If you are using Jetpack Navigation Compose, you'd get these
    // from the NavBackStackEntry arguments.
) {
    // --- ViewModel Instantiation using the new Factory ---
    val context = LocalContext.current
    val application = context.applicationContext as Application // Get Application instance
//    val factory = ChatViewModelFactory(application) // Create the factory

    // Get ViewModel using the factory
    val chatViewModel: ChatViewModel = hiltViewModel()
    // --- End ViewModel Instantiation ---

    // Collect states (remains the same)
    val chatHistory by chatViewModel.chatHistory.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val isRecording by chatViewModel.isRecording.collectAsStateWithLifecycle() // Collect recording state

    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Permission Handling (remains the same) ---
    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    var showRationaleDialog by remember { mutableStateOf(false) }

    // --- Load Data using LaunchedEffect ---
    // This effect runs when userId or conversationId changes (or on initial composition)
    LaunchedEffect(userId, conversationId) {
        Log.d("ChatScreen", "LaunchedEffect running: Loading data for User $userId, Conversation $conversationId")
        chatViewModel.loadDataForConversation(userId, conversationId)
    }
    // --- End Load Data ---


    // Effect to scroll down (remains the same)
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                // A small delay can sometimes help ensure the item is rendered before scrolling
                // kotlinx.coroutines.delay(100)
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    // --- UI Structure (Scaffold, TopBar, BottomBar structure remains the same) ---
    Scaffold(
        topBar = {
            // You might want to display the conversation ID or user info here
            TopAppBar(title = { Text("Chat") }) // Example Title
        },
        bottomBar = {
            ChatInputArea(
                userInput = userInput,
                onUserInputChanged = { userInput = it },
                onSendMessage = {
                    if (userInput.isNotBlank()) {
                        chatViewModel.sendMessage(userInput) // ViewModel handles saving now
                        userInput = ""
                        keyboardController?.hide()
                    }
                },
                isLoading = isLoading,
                isRecording = isRecording, // Pass recording state
                onRecordStart = {
                    // Check/Request permission before starting
                    if (recordAudioPermissionState.status.isGranted) {
                        chatViewModel.startRecordingAudio()
                    } else if (recordAudioPermissionState.status.shouldShowRationale) {
                        // Show rationale if needed (user denied before)
                        showRationaleDialog = true
                    } else {
                        // Request permission for the first time or if denied permanently
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                },
                onRecordStop = {
                    // Send with default prompt, or use userInput if not blank
                    val prompt = userInput.ifBlank { "Describe this audio" } // Changed default prompt slightly
                    chatViewModel.stopRecordingAudioAndSend(prompt) // ViewModel handles saving now
                    userInput = "" // Clear input after sending audio too
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat history display logic (remains largely the same)
            // The LazyColumn will now be populated based on the Flow from the database
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(chatHistory) { message ->
                    ChatBubble(message = message) // ChatBubble remains the same
                }

                // Loading/Recording indicators (remain the same)
                if (isLoading && chatHistory.isEmpty()) { // Show loading only if history is empty
                    item {
                        Box(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            Text("Loading History...", modifier = Modifier.padding(top = 60.dp)) // Indicate loading history
                        }
                    }
                } else if (isLoading && !isRecording) { // Show smaller loading indicator for AI response
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 5.dp), // Align left like AI bubbles
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }

                if (isRecording) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).background(Color.Red.copy(alpha = 0.1f)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(Icons.Filled.Mic, contentDescription = "Recording", tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Recording...", color = Color.Red)
                        }
                    }
                }
            }

            // Show initial prompts only if explicitly not loading AND history is empty
            if (chatHistory.isEmpty() && !isLoading && !isRecording) {
                InitialPrompts(onPromptClick = { prompt ->
                    // Ensure ViewModel is ready before sending initial prompt
                    chatViewModel.sendMessage(prompt)
                })
            }
        }

        // --- Rationale Dialog (remains the same) ---
        if (showRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showRationaleDialog = false },
                title = { Text("Permission Required") },
                text = { Text("Audio recording permission is needed to record messages. Please grant the permission.") },
                confirmButton = {
                    Button(onClick = {
                        showRationaleDialog = false
                        recordAudioPermissionState.launchPermissionRequest()
                    }) { Text("Grant") }
                },
                dismissButton = {
                    Button(onClick = { showRationaleDialog = false }) { Text("Deny") }
                }
            )
        }
    }
}

// --- ChatInputArea composable definition remains the same ---
@Composable
fun ChatInputArea(
    // ... parameters ...
    userInput: String,
    onUserInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean, // New state
    onRecordStart: () -> Unit, // Callback for start
    onRecordStop: () -> Unit // Callback for stop
) {
    // ... implementation remains the same ...
    val interactionSource = remember { MutableInteractionSource() } // Keep this definition
    val isMicPressed by interactionSource.collectIsPressedAsState()

    // Trigger start/stop based on press state changes
    LaunchedEffect(isMicPressed) {
        if (isMicPressed) {
            onRecordStart()
        } else {
            // Only trigger stop if we were actually recording
            if (isRecording) {
                onRecordStop()
            }
        }
    }

    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type or hold mic...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (!isRecording) onSendMessage() }),
                enabled = !isRecording,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { /* Interaction handled by LaunchedEffect */ },
                interactionSource = interactionSource,
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (isRecording) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ),
                enabled = !isLoading
            ) {
                Icon( // Use Icon directly for simpler tinting
                    Icons.Filled.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendMessage,
                enabled = userInput.isNotBlank() && !isLoading && !isRecording,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send message")
            }
        }
    }
}


// --- InitialPrompts Composable definition remains the same ---
@Composable
fun InitialPrompts(onPromptClick: (String) -> Unit) {
    // ... implementation remains the same ...
    val prompts = listOf(
        "Explain quantum physics",
        "Explain black holes simply",
        "Write a tweet about global warming",
        "Write a poem about love and roses",
        "How do you say 'How are you?' in German?",
        "Translate this text: ..."
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center prompts vertically
    ) {
        item {
            Text("Try asking:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp), textAlign = TextAlign.Center)
        }
        items(prompts) { prompt ->
            SuggestionChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt, textAlign = TextAlign.Center) }, // Center text in chip
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.9f) // Make chips wider
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) } // Add some space at the bottom
        item {
            Text("...or hold the Mic button to speak!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

// --- ChatBubble Composable definition remains the same ---
@Composable
fun ChatBubble(message: ChatMessage) {
    // ... implementation remains the same ...
    val bubbleColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer
    else if (message.isError) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (message.isFromUser) MaterialTheme.colorScheme.onPrimaryContainer
    else if (message.isError) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bubbleColor,
            modifier = Modifier
                .align(alignment)
                .padding(
                    start = if (message.isFromUser) 40.dp else 0.dp,
                    end = if (message.isFromUser) 0.dp else 40.dp
                ) // Indent opposite side
                .wrapContentWidth() // Don't stretch bubble
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}