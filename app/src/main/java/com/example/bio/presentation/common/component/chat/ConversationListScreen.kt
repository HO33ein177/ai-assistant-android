package com.example.bio.presentation.common.component.chat

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.bio.AppDestinations
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    navController: NavController,
    userId: Int
) {
    // Get Application context
    val application = LocalContext.current.applicationContext as Application
    // Create Factory
//    val factory = ConversationListViewModelFactory(application)
    // Get ViewModel
    val viewModel: ConversationListViewModel = hiltViewModel()

    // Collect state
    val conversationIds by viewModel.conversationIds.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Load conversations when the screen is composed or userId changes
    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Chats") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Create a new conversation ID
                val newConversationId = UUID.randomUUID().toString()
                // Navigate to ChatScreen with the new ID
                navController.navigate(AppDestinations.createChatRoute(userId, newConversationId))
            }) {
                Icon(Icons.Filled.Add, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (conversationIds.isEmpty()) {
                Text(
                    "No conversations yet. Tap the '+' button to start!",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(conversationIds) { conversationId ->
                        ConversationListItem(
                            conversationId = conversationId,
                            onClick = {
                                navController.navigate(AppDestinations.createChatRoute(userId, conversationId))
                            }
                        )
                        Divider() // Add a divider between items
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListItem(
    conversationId: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                // Display a truncated version of the ID or fetch first message later
                text = "Chat: ...${conversationId.takeLast(6)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(Icons.Filled.Chat, contentDescription = "Chat") // Add a chat icon
        }
        // You could add supporting content like last message timestamp later
    )
}