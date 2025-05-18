package com.example.bio.presentation.common.component.auth.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bio.AppDestinations
import com.example.bio.R
import com.example.bio.presentation.common.component.reusable.GradientBox
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton

private const val TAG = "LoginScreen"
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(), // Get LoginViewModel via Hilt
) {

    // Read state from ViewModel
    val email by viewModel.email
    val password by viewModel.password
    val loginState by viewModel.loginState // Observe the login process state

    val context = LocalContext.current
    val isLoading = loginState is LoginResult.Loading // Check if loading

    // Handle Login State changes (Side Effects for navigation/toast)
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginResult.Success -> {
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                // Generate a new conversation ID
                // Uses the SAME ID every time
                val commonConversationId = "GLOBAL_CHAT_ID" // Or any fixed string you choose
                val userId = state.userId

                Log.d(TAG, "Login successful. Navigating to ChatScreen with userId: $userId, conversationId: $commonConversationId")

                // Navigate to Chat Screen instead of Conversation List
                // Clear the login screen and anything before it from backstack

                navController.navigate(AppDestinations.createChatRoute(userId, commonConversationId)) {
                    // Try popping only the login screen itself
                    popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                }
                viewModel.resetLoginState() // Reset state after navigation handled
            }
            is LoginResult.Error -> {
                // Show error message to the user via Toast
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // ViewModel resets state when user types again.
            }
            LoginResult.Loading -> { /* UI handled by isLoading */ }
            LoginResult.Idle -> { /* Initial state */ }
        }
    }


    Scaffold { paddingValues -> // Use paddingValues from Scaffold

        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f), // Keep layout structure
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo and Title
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Soundwave",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    ),
                    // Use a color defined in your resources or theme
                    color = colorResource(R.color.purple_700)
                )

                Image(
                    painter = painterResource(id = R.drawable.logo), // logo
                    contentDescription = "logo",
                    modifier = Modifier.size(160.dp)
                )
            }

            GradientBox(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.3f) // layout structure
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        text = "ورود به حساب کاربری", // Login to Account
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.vazirmatn_bold)), // font
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = colorResource(R.color.black) // theme color
                    )

                    Spacer(Modifier.height(16.dp))
                    // Email Field
                    MyBasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        value = email,
                        label = "آدرس ایمیل", // Email Address
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(),
                        trailingIcon = Icons.Outlined.Email,
                        onValueChange = viewModel::changeEmail, // Update ViewModel using function reference
                        // indicate error state based on ViewModel
                        isError = loginState is LoginResult.Error
                    )

                    Spacer(Modifier.height(24.dp))

                    // Password Field
                    MyBasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        value = password,
                        label = "رمز عبور", // Password
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password // Password type
                        ),
                        keyboardActions = KeyboardActions(),
                        isPassword = true,
                        trailingIcon = Icons.Outlined.Lock,
                        onValueChange = viewModel::changePassword, // Update ViewModel using function reference
                        // indicate error state based on ViewModel
                        isError = loginState is LoginResult.Error
                    )

                    Spacer(Modifier.height(24.dp))

                    //  Login Button
                    RoundedButton(
                        text = if (isLoading) "در حال ورود..." else "ورود", // Login
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = !isLoading, // Disable button when loading
                        onClick = {
                            // Trigger login attempt in ViewModel
                            viewModel.attemptLogin()
                        }
                    )

                    // Links
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Forgot Password Link
                        Text(
                            modifier = Modifier
                                .clickable {
                                    // Navigate using routes from AppDestinations
                                    navController.navigate(AppDestinations.FORGET_PASSWORD_ROUTE)
                                },
                            text = "رمز عبور را فراموش کرده اید؟", // "Forgot Password?"
                            color = colorResource(R.color.purple_700),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                            )
                        )

                        // Signup Link
                        Text(
                            modifier = Modifier.clickable {
                                // Navigate using routes from AppDestinations
                                navController.navigate(AppDestinations.SIGNUP_ROUTE)
                            },
                            text = "ایجاد حساب کاربری", // "Create Account"
                            color = colorResource(R.color.purple_700),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                            )
                        )
                    }
                }
            }
        }
    }
}
