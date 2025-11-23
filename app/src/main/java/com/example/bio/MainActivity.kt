package com.example.bio // Ensure correct package

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bio.presentation.common.component.auth.change_password.ChangePasswordScreen
import com.example.bio.presentation.common.component.auth.login.LoginScreen
import com.example.bio.presentation.common.component.auth.signup.SignupScreen
import com.example.bio.presentation.common.component.chat.ChatScreen
import com.example.bio.presentation.common.component.chat.ConversationListScreen
import com.example.bio.presentation.common.component.landing.SimpleLandingScreen
import com.example.bio.presentation.common.component.onboarding.OnboardingScreen
import com.example.bio.presentation.common.component.splash.SplashScreen
import com.example.bio.presentation.common.component.theme.BioTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
private const val TAG = "AppNavigation" // <<< Add TAG for logging

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Call the NavHost setup
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Set the start destination.
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE // Start with Splash
    ) {
        // Splash Screen
        composable(route = AppDestinations.SPLASH_ROUTE) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500) // Simulate splash duration
                // Navigate to LOGIN after splash
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } // Remove Splash from back stack
                }
            }
            // Display your SplashScreen composable here
            SplashScreen(navController = navController) // Assuming SplashScreen takes NavController
        }

        // Onboarding Screen - Navigates to Login (handled internally)
        composable(route = AppDestinations.ONBOARDING_ROUTE) {
            // OnboardingScreen's internal button should navigate to LOGIN_ROUTE
            // and pop itself off the stack.
            OnboardingScreen(navController = navController)
        }

        // Login Screen - Navigates to Conversation List on Success
        composable(route = AppDestinations.LOGIN_ROUTE) {
            LoginScreen(navController = navController)
        }

        // Signup Screen - Navigates to Conversation List on Success
        composable(route = AppDestinations.SIGNUP_ROUTE) {
            SignupScreen(
                navController = navController,
                onSignupSuccess = { userId -> // userId is Long from ViewModel
                    Log.d("Navigation", "Signup Success! Navigating with userId: $userId")
                    try {
                        // Generate a new conversation ID
                        val newConversationId = UUID.randomUUID().toString()
                        // Navigate to Chat Screen instead of Conversation List
                        val route = AppDestinations.createChatRoute(userId.toInt(), newConversationId)
                        Log.d("Navigation", "Navigating to route: $route")
                        navController.navigate(route) {
                            // Clear signup and login screens from the back stack
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                            Log.d("Navigation", "Navigation executed.")
                        }
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error during signup navigation", e)
                    }
                }
            )
        }

        composable(route = AppDestinations.FORGET_PASSWORD_ROUTE) {
            // ChangePasswordScreen handles the reset flow
            ChangePasswordScreen(navController = navController)
        }
        // Conversation List Screen - Navigates to Chat or Settings
        composable(
            route = AppDestinations.CONVERSATION_LIST_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.USER_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(NavArguments.USER_ID)
            if (userId != null) {
                ConversationListScreen(navController = navController, userId = userId)
            } else {
                Text("Error: Missing User ID for Conversation List.")
                // Navigate back to login if userId is somehow missing
                LaunchedEffect(Unit) {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(navController.graph.id) { inclusive = true } // Clear broken backstack
                    }
                }
            }
        }

        // Chat Screen
        composable(
            route = AppDestinations.CHAT_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.USER_ID) { type = NavType.IntType },
                navArgument(NavArguments.CONVERSATION_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(NavArguments.USER_ID)
            val conversationId = backStackEntry.arguments?.getString(NavArguments.CONVERSATION_ID)

            Log.d(TAG, "Attempting to navigate to ChatScreen. Arguments received - userId: $userId, conversationId: $conversationId")


            if (userId != null && conversationId != null) {
                ChatScreen(
                    userId = userId,
                    conversationId = conversationId,
                    // Pass navController if ChatScreen needs it later:
                     navController = navController
                )
            } else {
                Log.e(TAG, "Error navigating to ChatScreen: Missing required arguments. userId: $userId, conversationId: $conversationId")

                Text("Error: Missing required arguments for chat.")
                // Navigate back if arguments are missing
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(
            route = AppDestinations.SIMPLE_LANDING_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.USER_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(NavArguments.USER_ID)

            Log.d(TAG, "Attempting to compose SimpleLandingScreen. Argument received - userId: $userId")

            if (userId != null) {
                SimpleLandingScreen(userId = userId)
            } else {
                Log.e(TAG, "Error navigating to SimpleLandingScreen: Missing userId argument.")
                Text("Error: Missing User ID for Landing Screen.")
                // Navigate back or to login if userId is missing
                LaunchedEffect(Unit) {
                    navController.popBackStack(AppDestinations.LOGIN_ROUTE, inclusive = false)
                }
            }
        }

    }
}