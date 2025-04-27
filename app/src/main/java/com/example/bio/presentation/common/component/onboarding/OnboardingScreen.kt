package com.example.bio.presentation.common.component.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bio.AppDestinations
import com.example.bio.R

@Composable
fun OnboardingScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes button to bottom
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(60.dp)) // Space from top

            // Title (Using English placeholder for Farsi)
            Text(
                text = "Your Smart Assistant", // Replace with Farsi text if possible
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF3F51B5) // Blue color
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Description (Using English placeholder for Farsi)
            Text(
                text = "Using this app, you can ask your questions via voice and receive text responses.", // Replace with Farsi
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder for the illustration
            // Replace with your actual illustration
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
        }


        // Button at the bottom
        Button(
            onClick = {
                // Navigate to chat and remove onboarding from back stack
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(AppDestinations.ONBOARDING_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp), // Space from bottom edge
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
        ) {
            // Text (Using English placeholder for Farsi "ادامه")
            Text("Continue", modifier = Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = "Continue")
        }
    }
}