package com.example.bio // Ensure correct package

import android.os.Bundle
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
import com.example.bio.presentation.common.component.onboarding.OnboardingScreen
import com.example.bio.presentation.common.component.splash.SplashScreen
import com.example.bio.presentation.common.component.theme.BioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioTheme { // Apply the BioTheme
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

    // Set the start destination. Assuming the app always starts with Login
    // after the initial Splash/Onboarding flow is handled.
    // You might add logic later to check for a logged-in user and start elsewhere.
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE // Start with Splash
    ) {
        // Splash Screen - Navigates to Login
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
//            LoginScreen( // Use the LoginScreen adapted from user's UI
//                navController = navController,
//                onLoginSuccess = { userId ->
//                    // On successful login, navigate to Conversation List
//                    navController.navigate(AppDestinations.createConversationListRoute(userId)) {
//                        // Clear login and any previous screens (like Splash/Onboarding) from backstack
//                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                        // Set Conversation List as the new root
//                        graph.setStartDestination(AppDestinations.CONVERSATION_LIST_ROUTE)
//
//                    }
//                }
//            )
        }

        // Signup Screen - Navigates to Conversation List on Success
        composable(route = AppDestinations.SIGNUP_ROUTE) {
            SignupScreen( // Use the SignupScreen generated previously
                navController = navController,
                onSignupSuccess = { userId ->
                    // On successful signup, navigate to Conversation List
                    navController.navigate(AppDestinations.createConversationListRoute(userId)) {
                        // Clear signup and login from backstack
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        // Set Conversation List as the new root if needed (might already be handled by popping login)
                        // graph.setStartDestination(AppDestinations.CONVERSATION_LIST_ROUTE)
                    }
                }
            )
        }

        composable(route = AppDestinations.FORGET_PASSWORD_ROUTE) {
            // Assuming ChangePasswordScreen handles the reset flow
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
                // Should not happen if navigation is correct, but handle defensively
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

            if (userId != null && conversationId != null) {
                ChatScreen(
                    userId = userId,
                    conversationId = conversationId
                    // Pass navController if ChatScreen needs it later:
                    // navController = navController
                )
            } else {
                Text("Error: Missing required arguments for chat.")
                // Navigate back if arguments are missing
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        // Settings Screen
        composable(
            route = AppDestinations.CHAT_ROUTE,
            arguments = listOf(
                navArgument(NavArguments.USER_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(NavArguments.USER_ID)
            if (userId != null) {
//                SettingsScreen(navController = navController, userId = userId)
            } else {
                Text("Error: Missing User ID for Settings.")
                // Navigate back if arguments are missing
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
    }
}