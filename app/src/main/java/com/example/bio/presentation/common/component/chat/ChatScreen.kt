package com.example.bio.presentation.common.component.chat

import android.Manifest
import android.app.Application
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
import androidx.compose.material.icons.filled.History // History icon
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.colorResource // Import for colorResource
import androidx.compose.ui.res.painterResource // Import for painterResource
import androidx.compose.ui.text.font.FontWeight // Import for FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow // Import for TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import com.example.bio.R // Make sure this import is correct for your project
import com.example.bio.ui.theme.vazirmatn // Make sure this import is correct for your project
import androidx.compose.animation.slideInHorizontally // Ensure these are imported
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Menu // This import might be needed if you use Menu icon elsewhere


// --- Define a data class for your history items ---
data class HistoryItem(
    val id: Int, // A unique identifier for each history item
    val text: String,
    val dateGroup: String // e.g., "امروز", "دیروز", "7 روز پیش"
    // Add other relevant data like conversation ID if needed
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    // Add parameters to receive userId and conversationId
    userId: Int,
    conversationId: String
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val chatViewModel: ChatViewModel = hiltViewModel()

    val chatHistory by chatViewModel.chatHistory.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val isRecording by chatViewModel.isRecording.collectAsStateWithLifecycle()

    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    var showRationaleDialog by remember { mutableStateOf(false) }

    // --- State to control History_page visibility ---
    var isHistoryPageOpen by remember { mutableStateOf(false) }

    // --- Load Data using LaunchedEffect ---
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

    // Use Box to layer the main content and the History_page (as a drawer)
    Box(modifier = Modifier.fillMaxSize()) {
        // --- Main Chat Content (Scaffold) ---
        // This content is always present, History_page will overlay it
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat") }, // Example Title
                    actions = {
                        // History Icon button - toggles History_page visibility
                        IconButton(onClick = {
                            isHistoryPageOpen = true // Open the History_page
                        }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History"
                            )
                        }
                    }
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

        // --- History Page (as a Drawer) ---
        // Pass the state and a close callback to History_page
        History_page(
            visible = isHistoryPageOpen,
            onClose = { isHistoryPageOpen = false },
            // You might pass a callback here to handle loading the selected chat
            onHistoryItemSelected = { selectedItem ->
                Log.d("ChatScreen", "History item selected: ${selectedItem.text}")
                // TODO: Implement logic to load the chat for selectedItem.id
                // You may want to close the drawer automatically after selection
                isHistoryPageOpen = false
            }
        )
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

// --- ChatInputArea composable definition remains the same ---
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
            onRecordStart()
        } else {
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
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text("Try asking:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp), textAlign = TextAlign.Center)
        }
        items(prompts) { prompt ->
            SuggestionChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt, textAlign = TextAlign.Center) },
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.9f)
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Text("...or hold the Mic button to speak!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

// --- ChatBubble Composable definition remains the same ---
@Composable
fun ChatBubble(message: ChatMessage) {
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
                )
                .wrapContentWidth()
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}


// --- Modified History_page Composable ---
// It now takes 'visible' and 'onClose' parameters, and manages selected item state
@Composable
fun History_page(
    visible: Boolean, // State from parent to control visibility
    onClose: () -> Unit, // Callback to notify parent to close the drawer
    onHistoryItemSelected: (HistoryItem) -> Unit // Callback to notify parent when a history item is selected
) {
    // State to keep track of the currently selected history item's ID
    var selectedHistoryItemId by remember { mutableStateOf<Int?>(null) }

    // --- Sample History Data (Replace with your actual data source) ---
    // This structure groups items by date
    val historyItemsGrouped = remember {
        listOf(
            "امروز" to listOf(
                HistoryItem(1, "فیگما چیست؟", "امروز"),
                HistoryItem(2, "چرا یادگیری اندروید استودیو سخت است", "امروز"),
                HistoryItem(3, "طراحی ui", "امروز")
            ),
            "دیروز" to listOf(
                HistoryItem(4, "فرمول ساخت اتم", "دیروز"),
                HistoryItem(5, "تمرین QR code", "دیروز")
            ),
            "7 روز پیش" to listOf(
                HistoryItem(6, "کد python", "7 روز پیش"),
                HistoryItem(7, "دانشگاه اصفهان", "7 روز پیش"),
                HistoryItem(8, "خرابی سیستم", "7 روز پیش"),
                HistoryItem(9, "یادگیری کاتلین", "7 روز پیش"),
                HistoryItem(10, "ایجاد تب جدید در برنامه اندروید استودیو", "7 روز پیش")
            )
        )
    }


    // The Box containing the overlay and the animated drawer content
    // This Box will cover the whole screen when visible
    Box(modifier = Modifier.fillMaxSize()) {

        // Semi-transparent background overlay - Visible only when the drawer is open
        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() } // Close drawer when clicking outside
            )
        }

        // Animated Drawer Content - Slides in/out based on the 'visible' state
        AnimatedVisibility(
            visible = visible, // Use the state passed from the parent
            enter = slideInHorizontally(
                initialOffsetX = { -it }, // Slide from the left
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it }, // Slide out to the left
                animationSpec = tween(300)
            )
        ) {
            // The actual content of the sidebar (drawer)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.7f) // Sidebar width is 70% of screen width
                    .background(MaterialTheme.colorScheme.surface) // Use theme surface color
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)) // Add rounded corners on the visible side
                    .padding(16.dp)
                    .clickable(enabled = false) { } // Prevent clicks on the drawer content from closing the drawer via overlay
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = 16.dp) // Adjusted top padding
                ) {
                    // Header Row (Close Button, Icon, Title)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // Close Button
                        IconButton(onClick = { onClose() }) // Call the onClose callback
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = "Close History",
                                tint = colorResource(id = R.color.black),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Robot Icon
                        Image(
                            painter = painterResource(id = R.drawable.robo_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(45.dp)
                        )

                        // Title "سوابق"
                        Text(
                            text = "سوابق",
                            color = colorResource(id = R.color.purple_700), // Using purple_700 as in your example
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = vazirmatn,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }

                    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // --- History Items List ---
                    // Use LazyColumn to efficiently display the history items
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        historyItemsGrouped.forEach { (dateGroup, items) ->
                            item {
                                // Display the date group title
                                History_title_row(dateGroup)
                            }
                            items(items, key = { it.id }) { item ->
                                // Display each history item row
                                History_row(
                                    item = item, // Pass the whole item
                                    isSelected = item.id == selectedHistoryItemId, // Check if this item is selected
                                    onItemClick = { clickedItem ->
                                        // Update the selected item ID when a row is clicked
                                        selectedHistoryItemId = clickedItem.id
                                        // Notify the parent (ChatScreen) that an item was selected
                                        onHistoryItemSelected(clickedItem)
                                        // Optionally close the drawer after selection
                                        // onClose()
                                    }
                                )
                            }
                        }
                    }


                    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // User Profile Info
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.main_blue)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = Color.White,
                                fontSize = 24.sp,
                            )
                        }

                        Text(
                            text = "Ali khorasani2002",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// --- Modified History_row Composable ---
// It now receives the item data, its selection state, and a click callback
@Composable
fun History_row(
    item: HistoryItem, // Receive the history item data
    isSelected: Boolean, // Receive whether this item is currently selected
    onItemClick: (HistoryItem) -> Unit // Callback to notify parent when clicked
) {
    // Removed the internal 'hover' state

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)
    {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(if (isSelected) colorResource(id = R.color.purple_700) else Color.Transparent)
                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .clickable {
                    onItemClick(item)
                },
        ) {
            Text(
                text = item.text,
                fontSize = 18.sp,
                fontFamily = vazirmatn,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun History_title_row(text: String) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)
    {
        val formattedText = remember {
            BidiFormatter.getInstance().unicodeWrap(text, TextDirectionHeuristics.RTL)
        }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 12.dp)
        ) {
            Text(
                text = formattedText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirmatn,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}