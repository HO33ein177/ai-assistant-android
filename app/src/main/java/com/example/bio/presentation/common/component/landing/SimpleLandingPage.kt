package com.example.bio.presentation.common.component.landing // Adjust package as needed

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

private const val TAG = "SimpleLandingScreen"

@Composable
fun SimpleLandingScreen(userId: Int) {
    // Log that the screen is composing
    LaunchedEffect(Unit) {
        Log.d(TAG, ">>> SimpleLandingScreen Composed Successfully for userId: $userId <<<")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Navigation Successful!")
            Text("Welcome, User ID: $userId")
        }
    }
}