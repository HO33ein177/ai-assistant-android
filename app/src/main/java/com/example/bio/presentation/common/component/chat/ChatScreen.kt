package com.example.bio.presentation.common.component.chat

import android.Manifest
import android.text.BidiFormatter
import android.text.TextDirectionHeuristics
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.bio.AppDestinations
import com.example.bio.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


// Data class for representing a conversation summary in the history list
// (Ensure this is defined, e.g., in presentation.common.component.chat or data.model)
 data class ConversationSummary(
    val conversationId: String,
    val lastMessageTimestamp: Long,
    val firstMessageContent: String?
 )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    navController: NavController,
    userId: Int,
    conversationId: String
) {
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = hiltViewModel()
    val conversationListViewModel: ConversationListViewModel = hiltViewModel()

    val chatHistory by chatViewModel.chatHistory.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val isRecording by chatViewModel.isRecording.collectAsStateWithLifecycle()

    val conversationSummaries by conversationListViewModel.conversationSummaries.collectAsStateWithLifecycle()
    val isHistoryLoading by conversationListViewModel.isLoading.collectAsStateWithLifecycle()


    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    var showRationaleDialog by remember { mutableStateOf(false) }
    var isHistoryPageOpen by remember { mutableStateOf(false) }

    LaunchedEffect(userId, conversationId) {
        Log.d("ChatScreen", "LaunchedEffect: Loading data for User $userId, Current Conversation $conversationId")
        chatViewModel.loadDataForConversation(userId, conversationId)
    }

    LaunchedEffect(userId, isHistoryPageOpen) {
        if (isHistoryPageOpen) {
            Log.d("ChatScreen", "LaunchedEffect: History page opened. Loading conversations for User $userId")
            conversationListViewModel.loadConversationSummaries(userId)
        }
    }

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Chat: ...${conversationId.takeLast(6)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            isHistoryPageOpen = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                ChatInputArea(
                    userInput = userInput,
                    onUserInputChanged = { userInput = it },
                    onSendMessage = {
                        if (userInput.isNotBlank()) {
                            chatViewModel.sendMessage(userInput)
                            userInput = ""
                            keyboardController?.hide()
                        }
                    },
                    isLoading = isLoading,
                    isRecording = isRecording,
                    onRecordStart = {
                        if (recordAudioPermissionState.status.isGranted) {
                            chatViewModel.startRecordingAudio()
                        } else if (recordAudioPermissionState.status.shouldShowRationale) {
                            showRationaleDialog = true
                        } else {
                            recordAudioPermissionState.launchPermissionRequest()
                        }
                    },
                    onRecordStop = {
                        val prompt = userInput.ifBlank { "Describe this audio" }
                        chatViewModel.stopRecordingAudioAndSend(prompt)
                        userInput = ""
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatHistory) { message ->
                        ChatBubble(message = message)
                    }

                    if (isLoading && chatHistory.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                                Text("Loading History...", modifier = Modifier.padding(top = 60.dp))
                            }
                        }
                    } else if (isLoading && !isRecording) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 5.dp),
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

                if (chatHistory.isEmpty() && !isLoading && !isRecording) {
                    InitialPrompts(onPromptClick = { prompt ->
                        chatViewModel.sendMessage(prompt)
                    })
                }
            }
        }

        HistoryPage(
            visible = isHistoryPageOpen,
            onClose = { isHistoryPageOpen = false },
            conversationSummaries = conversationSummaries,
            isLoading = isHistoryLoading,
            onHistoryItemSelected = { selectedConvId ->
                Log.d("ChatScreen", "History item selected: $selectedConvId")
                if (selectedConvId != conversationId) {
                    navController.navigate(AppDestinations.createChatRoute(userId, selectedConvId)) {
                        // Pop up to the login route to clear the current chat from the back stack
                        // when navigating to a different existing chat.
                        // This makes the back button from the newly opened chat go to Login (or whatever is before it).
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = false }
                    }
                }
                isHistoryPageOpen = false
            },
            onNewChatClicked = { newConvId -> // Handle new chat click
                Log.d("ChatScreen", "New chat clicked. Navigating to new conversation: $newConvId")
                navController.navigate(AppDestinations.createChatRoute(userId, newConvId)) {
                    // Similar to selecting an existing chat, clear the current one if it's different
                    // or adjust based on desired backstack behavior for new chats.
                    // If the current screen IS the chat screen, popping up to LOGIN_ROUTE
                    // before navigating to a new chat instance will effectively replace it.
                    popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = false }
                }
                isHistoryPageOpen = false
            },
            currentUserId = userId
        )
    }

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

@Composable
fun ChatInputArea(
    userInput: String,
    onUserInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isMicPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isMicPressed) {
        if (isMicPressed) {
            if (!isRecording && !isLoading) onRecordStart()
        } else {
            if (isRecording) {
                onRecordStop()
            }
        }
    }

    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
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
                keyboardActions = KeyboardActions(onSend = { if (!isRecording && userInput.isNotBlank()) onSendMessage() }),
                enabled = !isRecording && !isLoading,
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { /* Interaction handled by LaunchedEffect */ },
                interactionSource = interactionSource,
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (isRecording) Color.Red.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                enabled = !isLoading
            ) {
                Icon(
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
                modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                colors = IconButtonDefaults.iconButtonColors(
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

@Composable
fun InitialPrompts(onPromptClick: (String) -> Unit) {
    val prompts = listOf(
        "Explain quantum physics",
        "Explain black holes simply",
        "Write a tweet about global warming",
        "Write a poem about love and roses",
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Try asking:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        prompts.forEach { prompt ->
            SuggestionChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt, textAlign = TextAlign.Center) },
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.9f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "...or hold the Mic button to speak!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer
    else if (message.isError) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (message.isFromUser) MaterialTheme.colorScheme.onPrimaryContainer
    else if (message.isError) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val isRtl = message.text.any { it in '\u0600'..'\u06FF' }
    val textAlignment = if (isRtl) TextAlign.Right else TextAlign.Left
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr


    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isFromUser) 16.dp else 4.dp,
                    topEnd = if (message.isFromUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = bubbleColor,
                modifier = Modifier
                    .align(alignment)
                    .padding(
                        start = if (message.isFromUser) 40.dp else 0.dp,
                        end = if (message.isFromUser) 0.dp else 40.dp
                    )
                    .wrapContentWidth()
                    .widthIn(min = 60.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = textAlignment
                )
            }
        }
    }
}


@Composable
fun HistoryPage(
    visible: Boolean,
    onClose: () -> Unit,
    conversationSummaries: List<ConversationSummary>,
    isLoading: Boolean,
    onHistoryItemSelected: (String) -> Unit,
    onNewChatClicked: (String) -> Unit, // New callback for starting a new chat
    currentUserId: Int
) {
    var selectedConversationId by remember { mutableStateOf<String?>(null) }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp))
    }

    Box(modifier = Modifier.fillMaxSize()) { // Main container for the drawer and overlay
        if (visible) {
            Box( // Overlay
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() }
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
        ) {
            // Drawer content using Scaffold to easily place FAB
            Scaffold(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface) // Explicit background for Scaffold
                    .clickable(enabled = false) { }, // Consume clicks on the drawer itself
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            val newConversationId = UUID.randomUUID().toString()
                            onNewChatClicked(newConversationId) // Call the new callback
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(bottom = 60.dp) // Ensure it's above the profile info if screen is short
                    ) {
                        Icon(Icons.Filled.Add, "Start New Chat")
                    }
                }
            ) { scaffoldPadding -> // Content of the drawer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding) // Apply padding from Scaffold
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp) // Additional internal padding
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                    ) {
                        IconButton(onClick = { onClose() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Chat History",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.robo_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )

                    if (isLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (conversationSummaries.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                "No chat history yet. Tap '+' to start!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            val todayStart = getStartOfDayMillis()
                            val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)
                            val grouped = conversationSummaries.groupBy { summary ->
                                when {
                                    summary.lastMessageTimestamp >= todayStart -> "Today"
                                    summary.lastMessageTimestamp >= yesterdayStart -> "Yesterday"
                                    else -> "Older"
                                }
                            }
                            val groupOrder = listOf("Today", "Yesterday", "Older")

                            groupOrder.forEach { dateGroup ->
                                grouped[dateGroup]?.let { itemsInGroup ->
                                    if (itemsInGroup.isNotEmpty()) {
                                        item {
                                            HistoryTitleRow(text = dateGroup)
                                        }
                                        items(itemsInGroup, key = { it.conversationId }) { summary ->
                                            HistoryRow(
                                                summary = summary,
                                                isSelected = summary.conversationId == selectedConversationId,
                                                onItemClick = {
                                                    selectedConversationId = it.conversationId
                                                    onHistoryItemSelected(it.conversationId)
                                                },
                                                formatTimestamp = ::formatTimestamp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // User Profile Info (at the bottom)
                    // This will be above the FAB due to Scaffold's layout behavior
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp) // Added bottom padding
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "User ID: $currentUserId",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

fun getStartOfDayMillis(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}


@Composable
fun HistoryRow(
    summary: ConversationSummary,
    isSelected: Boolean,
    onItemClick: (ConversationSummary) -> Unit,
    formatTimestamp: (Long) -> String
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    ListItem(
        headlineContent = {
            Text(
                text = summary.firstMessageContent?.take(100) ?: "Chat: ...${summary.conversationId.takeLast(8)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        },
        supportingContent = {
            Text(
                text = formatTimestamp(summary.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                Icons.Filled.Chat,
                contentDescription = "Chat Icon",
                tint = if (isSelected) contentColor else MaterialTheme.colorScheme.secondary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onItemClick(summary) }
            .padding(vertical = 4.dp),
        colors = ListItemDefaults.colors(
            containerColor = backgroundColor
        )
    )
}

@Composable
fun HistoryTitleRow(text: String) {
    val formattedText = remember(text) {
        BidiFormatter.getInstance().unicodeWrap(text, TextDirectionHeuristics.ANYRTL_LTR)
    }
    Text(
        text = formattedText,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}
