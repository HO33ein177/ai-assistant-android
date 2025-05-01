package com.example.bio.presentation.common.component.auth.change_password

import android.widget.Toast // Or use Snackbar for better UI feedback
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bio.presentation.common.component.reusable.MyBasicTextField // Ensure this import is correct
import com.example.bio.presentation.common.component.reusable.RoundedButton // Ensure this import is correct

// Make sure ResetStatus is accessible (e.g., defined in ViewModel file or its own file)
// import com.example.bio.presentation.common.component.auth.change_password.ResetStatus // If defined elsewhere


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    // Observe state from the ViewModel
    val email by viewModel.email
    val resetStatus by viewModel.resetStatus
    val context = LocalContext.current
    val isLoading = resetStatus is ResetStatus.Loading

    // Show feedback Toast/Snackbar based on resetStatus changes
    LaunchedEffect(resetStatus) {
        when (val status = resetStatus) {
            is ResetStatus.Success -> {
                Toast.makeText(context, "Password reset email sent successfully!", Toast.LENGTH_LONG).show()
                // Optional: Automatically navigate back after a short delay or keep the user here
                // kotlinx.coroutines.delay(2000)
                // navController.popBackStack()
                viewModel.resetStatusHandled() // Reset status after showing toast
            }
            is ResetStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                viewModel.resetStatusHandled() // Reset status after showing toast
            }
            else -> { /* Idle or Loading - No immediate feedback needed */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reset Password") })
            // Optional: Add navigation icon to go back
            // navigationIcon = {
            //     IconButton(onClick = { navController.popBackStack() }) {
            //         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            //     }
            // }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp), // Consistent padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Text(
                text = "Enter your email address",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "We'll send you an email with instructions to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Input Field
            MyBasicTextField(
                value = email,
                onValueChange = viewModel::onEmailChange, // Use correct function reference
                label = "Email Address",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = Icons.Outlined.Email,
                modifier = Modifier.fillMaxWidth(),
                isError = resetStatus is ResetStatus.Error // Show error state if failed
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Loading Indicator or Button
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(48.dp)) { // Reserve space
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    RoundedButton(
                        text = "Send Reset Email",
                        onClick = { viewModel.sendPasswordResetEmail() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() // Enable button only if email has text
                    )
                }
            }

            // Remove any logic or UI that previously displayed CodeScreen or PasswordScreen

            // Optional: Button to manually navigate back
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back to Login")
            }
        }
    }
}