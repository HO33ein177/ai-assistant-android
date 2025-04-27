package com.example.bio.presentation.common.component.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import com.example.bio.AppDestinations
import com.example.bio.R


@Composable
fun SplashScreen(navController: NavController) {
    // This effect runs once when the composable enters the composition
    LaunchedEffect(key1 = true) {
        delay(2000L) // Wait for 2 seconds
        // Navigate to onboarding and remove splash from back stack
        navController.navigate(AppDestinations.ONBOARDING_ROUTE) {
            popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3F51B5)), // Blue background similar to mockup
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder Icon - Replace with your actual robot logo if you have it
            Image(
                // OLD - Remove or comment out this line:
                // imageVector = Icons.Filled.Chat, // Placeholder

                // NEW - Add this line instead:
                painter = painterResource(id = R.drawable.onboarding_illustration), // Use your actual filename here!

                contentDescription = "Onboarding Illustration", // Keep or update this description
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Keep existing modifiers or adjust as needed
                    .aspectRatio(1f),   // Keep existing modifiers or adjust as needed
                contentScale = ContentScale.Fit, // ContentScale.Fit is usually good for illustrations

                // REMOVE or comment out the colorFilter line if you had one,
                // as tinting a PNG often isn't desired:
                // colorFilter = ColorFilter.tint(Color(0xFF3F51B5))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Soundwave",
                color = Color.White,
                fontSize = 32.sp
                // Add custom font if desired
            )
        }
    }
}