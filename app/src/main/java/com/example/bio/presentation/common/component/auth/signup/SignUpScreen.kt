package com.example.bio.presentation.common.component.auth.signup // Correct package


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bio.R
import com.example.bio.presentation.common.component.auth.SignupState
import com.example.bio.presentation.common.component.auth.UserViewModel
import com.example.bio.presentation.common.component.theme.BioTheme


@Composable
fun SignupScreen(
    navController: NavController,
    onSignupSuccess: (userId: Long) -> Unit, // Changed to Long to match ViewModel potentially
    viewModel: UserViewModel = hiltViewModel() // Get instance via Hilt
) {
    val context = LocalContext.current // For Toasts

    // --- Read State from ViewModel ---
    val username by viewModel.username
    val email by viewModel.email
    val password by viewModel.password
    val confirmPassword by viewModel.confirmPassword
    val signupState by viewModel.signupState

    // --- Local derived state ---
    val passwordsMatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword
    val fieldsNotEmpty = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
    val isLoading = signupState is SignupState.Loading
    // Extract error message *only* when state is Error
    val signupError = (signupState as? SignupState.Error)?.message

    // --- Handle Signup State Changes (Side Effects) ---
    LaunchedEffect(signupState) {
        when (val state = signupState) {
            is SignupState.Success -> {
                Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                onSignupSuccess(state.userId) // Trigger navigation
                viewModel.resetSignupState() // Reset state after navigation handled
            }
            is SignupState.Error -> {
                // Error message is displayed via the Text composable below
                // Toast.makeText(context, state.message, Toast.LENGTH_LONG).show() // Optional Toast
                viewModel.resetSignupState() // Reset state after showing error to allow retry
            }
            SignupState.Loading -> { /* UI handled by isLoading */ }
            SignupState.Idle -> { /* Initial state */ }
        }
    }

    BioTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Logo and Title ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(80.dp),
                    painter = painterResource(id = R.drawable.logo), // ASSUMES logo exists
                    contentDescription = "Logo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Soundwave",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "ایجاد حساب کاربری", // "Create Account"
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 30.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.W900
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- Username Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = username,
                // Call ViewModel update function
                onValueChange = viewModel::onUsernameChange,
                placeholder = { Text("نام کاربری", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // "Username"
//                leadingIcon = {
//                    Icon(
//                        painter = painterResource(id = R.drawable.profile_picture), // ASSUMES profile_icon exists
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = signupError != null // Indicate error if signup failed
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Email Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = email,
                // Call ViewModel update function
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("آدرس ایمیل", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // "Email Address"
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email_icon), // ASSUMES email_icon exists
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = signupError != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = password,
                // Call ViewModel update function
                onValueChange = viewModel::onPasswordChange,
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("رمز عبور", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // "Password"
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock_icon), // ASSUMES lock_icon exists
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = signupError != null || (password.isNotEmpty() && confirmPassword.isNotEmpty() && !passwordsMatch) // Show error if mismatched
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Repeat Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = confirmPassword,
                // Call ViewModel update function
                onValueChange = viewModel::onConfirmPasswordChange,
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("تکرار رمز عبور", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // "Repeat Password"
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock_icon), // ASSUMES lock_icon exists
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = signupError != null || (password.isNotEmpty() && confirmPassword.isNotEmpty() && !passwordsMatch) // Show error if mismatched
            )

            // Display validation/signup error message from ViewModel State
            if (signupError != null) {
                Text(
                    text = signupError, // Display error message from ViewModel
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End).padding(horizontal = 30.dp),
                    textAlign = TextAlign.Right
                )
            }
            // Display immediate password mismatch error
            else if (password.isNotEmpty() && confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "رمزهای عبور مطابقت ندارند", // "Passwords do not match"
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End).padding(horizontal = 30.dp),
                    textAlign = TextAlign.Right
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Signup Button ---
            Button(
                onClick = {
                    // Let ViewModel handle validation and creation
                    viewModel.handleSignup()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f),
                // Button enabled only if not loading AND passwords match (if both entered)
                enabled = !isLoading && (password.isEmpty() || confirmPassword.isEmpty() || passwordsMatch)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = "ثبت نام", // "Sign Up"
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W800
                    )
                }
            }

            // --- Link to Login ---
            Row(
                modifier = Modifier.fillMaxWidth(0.85f)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { navController.popBackStack() }) { // Go back to previous screen (Login)
                    Text(
                        text = "قبلا ثبت نام کرده اید؟ وارد شوید", // "Already have an account? Login"
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


// Helper function for consistent TextField colors (keep as is)
@Composable
fun signupTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
)

// Remove the History related composables and Preview from here
// @Preview ...
// fun Login_page_preview() { ... }