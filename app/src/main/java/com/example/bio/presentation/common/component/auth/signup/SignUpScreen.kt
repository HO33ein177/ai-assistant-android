package com.example.bio.presentation.common.component.auth.signup // Correct package

// Import necessary components from this project (bio)
import android.app.Application
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.bio.presentation.common.component.auth.UserViewModel
import com.example.bio.presentation.common.component.theme.BioTheme

// Assume vazirmatn font is NOT available in this project unless added separately

@Composable
fun SignupScreen( // Rename composable to match file name and convention
    navController: NavController,
    onSignupSuccess: (userId: Int) -> Unit // Callback for successful signup
) {
    // --- ViewModel Setup ---
    val application = LocalContext.current.applicationContext as Application
//    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = hiltViewModel()
    val context = LocalContext.current // For Toasts
    // --- End ViewModel Setup ---

    // States adapted from your 'Login_page' which is now Signup UI
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // Assuming username is needed for signup too

    // Remove state for sent_code unless you implement email verification
    // var sent_code = remember { mutableStateOf("") }

    // State for signup errors
    var signupError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Use this project's theme
    BioTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Use MaterialTheme colors instead of R.color
                .background(MaterialTheme.colorScheme.background) // Or surface? Check BioTheme
                .padding(vertical = 16.dp, horizontal = 16.dp), // Adjust padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp)) // Adjust spacing as needed

            // --- Logo and Title (adapted from your code) ---
            Row(
                // horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(80.dp), // Adjusted size
                    painter = painterResource(id = R.drawable.logo), // ASSUMES R.drawable.logo exists here
                    contentDescription = "Logo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Soundwave",
                    fontSize = 30.sp, // Adjusted size
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                    // fontFamily = vazirmatn // Removed
                )
            }

            Text(
                text = "ایجاد حساب کاربری", // "Create Account" (Keep your Farsi text)
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 30.dp),
                fontSize = 24.sp, // Adjusted size
                //fontFamily = vazirmatn, // Removed
                fontWeight = FontWeight.W900
            )
            // --- End Logo and Title ---

            Spacer(modifier = Modifier.height(30.dp))

            // --- Username Field (Assuming needed for signup) ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f), // Use consistent width
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("نام کاربری", // "Username"
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { // Example using leading icon
                    Icon(
                        painter = painterResource(id = R.drawable.profile_picture), // ASSUME profile_icon exists
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(), // Use helper function for colors
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = signupError != null // Basic error indication
            )
            Spacer(modifier = Modifier.height(16.dp)) // Consistent spacing

            // --- Email Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("آدرس ایمیل", // "Email Address"
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email_icon), // ASSUMES email_icon exists here
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
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("رمز عبور", // "Password"
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock_icon), // ASSUMES lock_icon exists here
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = signupError != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Repeat Field ---
            TextField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = passwordRepeat,
                onValueChange = { passwordRepeat = it },
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("تکرار رمز عبور", // "Repeat Password"
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock_icon), // ASSUMES lock_icon exists here
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = signupTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = signupError != null || (password.isNotEmpty() && passwordRepeat.isNotEmpty() && password != passwordRepeat)
            )

            // Display validation/signup error message
            if (signupError != null) {
                Text(
                    text = signupError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End).padding(horizontal = 30.dp), // Align text right if needed
                    textAlign = TextAlign.Right
                )
            }
            if (password.isNotEmpty() && passwordRepeat.isNotEmpty() && password != passwordRepeat) {
                Text(
                    text = "رمزهای عبور مطابقت ندارند", // "Passwords do not match"
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End).padding(horizontal = 30.dp),
                    textAlign = TextAlign.Right
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Spacing before button

            // --- Signup Button ---
            Button(
                onClick = {
                    signupError = null // Clear previous error
                    if (username.isBlank() || email.isBlank() || password.isBlank()) {
                        signupError = "لطفا تمام فیلدها را پر کنید" // "Please fill all fields"
                        return@Button
                    }
                    if (password != passwordRepeat) {
                        signupError = "رمزهای عبور مطابقت ندارند" // "Passwords do not match"
                        return@Button
                    }

                    // !!! TODO: Implement Password Hashing !!!
                    // val hashedPassword = hashPassword(password) // Use a library like BCrypt
                    val insecurePassword = password // Replace with hashedPassword

                    isLoading = true
                    // Check if username already exists before attempting creation
                    userViewModel.getUserByUsername(username) { existingUser ->
                        if (existingUser == null) {
                            // Username is available, proceed with creation
                            userViewModel.createUser(username, email, insecurePassword) { newUserId ->
                                isLoading = false
                                if (newUserId > 0) {
                                    Toast.makeText(context, "ثبت نام موفقیت آمیز بود!", Toast.LENGTH_SHORT).show() // "Signup successful!"
                                    onSignupSuccess(newUserId.toInt()) // Signup successful callback
                                } else {
                                    signupError = "ثبت نام انجام نشد. لطفا دوباره تلاش کنید." // "Signup failed. Please try again."
                                }
                            }
                        } else {
                            isLoading = false
                            signupError = "این نام کاربری قبلا گرفته شده است" // "Username already taken"
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // Use theme color
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f),
                enabled = !isLoading && password == passwordRepeat && password.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = "ثبت نام", // "Sign Up"
                        color = MaterialTheme.colorScheme.onPrimary,
                        //fontFamily = vazirmatn, // Removed
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W800
                    )
                }
            }
            // --- End Signup Button ---


            // --- Link to Login ---
            Row( // Using Row for better alignment options if needed later
                modifier = Modifier.fillMaxWidth(0.85f)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center // Center the link
            ) {
                TextButton(onClick = { navController.popBackStack() }) { // Go back to Login
                    Text(
                        text = "قبلا ثبت نام کرده اید؟ وارد شوید", // "Already have an account? Login"
                        color = MaterialTheme.colorScheme.primary,
                        //fontFamily = vazirmatn, // Removed
                        fontSize = 16.sp // Adjusted size
                    )
                }
            }
            // --- End Link to Login ---

            // Remove verification code section and contact us link from signup page
        }
    }
}


// Helper function for consistent TextField colors (optional)
@Composable
fun signupTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant, // Added
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant // Added
)


// Remove the History related composables and Preview from here
// @Preview ...
// fun Login_page_preview() { ... }