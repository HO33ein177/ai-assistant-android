package com.example.bio.presentation.common.component.chat

import android.util.Log
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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.bio.AppDestinations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    navController: NavController,
    userId: Int
) {
    // Get ViewModel using Hilt
    val viewModel: ConversationListViewModel = hiltViewModel()

    // Collect state from the ViewModel
    val conversationSummaries by viewModel.conversationSummaries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Load conversation summaries when the screen is composed or userId changes
    LaunchedEffect(userId) {
        Log.d("ConvListScreen", "Loading conversation summaries for userId: $userId")
        viewModel.loadConversationSummaries(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Create a new conversation ID
                    val newConversationId = UUID.randomUUID().toString()
                    Log.d("ConvListScreen", "FAB clicked. Navigating to new chat with userId: $userId, conversationId: $newConversationId")
                    // Navigate to ChatScreen with the new ID
                    navController.navigate(AppDestinations.createChatRoute(userId, newConversationId))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && conversationSummaries.isEmpty()) { // Show loader only if summaries are empty
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (conversationSummaries.isEmpty()) {
                Text(
                    "No conversations yet. Tap the '+' button to start!",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(conversationSummaries, key = { it.conversationId }) { summary ->
                        ConversationListItem(
                            summary = summary,
                            onClick = {
                                Log.d("ConvListScreen", "Item clicked. Navigating to chat with userId: $userId, conversationId: ${summary.conversationId}")
                                navController.navigate(AppDestinations.createChatRoute(userId, summary.conversationId))
                            }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListItem(
    summary: ConversationSummary,
    onClick: () -> Unit
) {
    // Function to format timestamp
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // Use local timezone
        return sdf.format(Date(timestamp))
    }

    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        headlineContent = {
            Text(
                text = summary.firstMessageContent?.take(100) ?: "Chat: ...${summary.conversationId.takeLast(8)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "Last message: ${formatTimestamp(summary.lastMessageTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                Icons.Filled.Chat,
                contentDescription = "Chat Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
