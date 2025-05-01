package com.example.bio.presentation.common.component.auth

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.UserDao
import com.example.bio.data.local.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// Sealed interface/class for Signup UI State
sealed interface SignupState {
    data object Idle : SignupState            // Initial state
    data object Loading : SignupState         // Signup in progress
    data class Success(val userId: Long) : SignupState // Signup successful, pass userId
    data class Error(val message: String) : SignupState // Signup failed
}


private const val TAG = "UserViewModel"

// Data class to hold combined user info (Firebase + Local)
data class CombinedUserInfo(
    val firebaseUser: FirebaseUser?, // Firebase user object (can be null if logged out)
    val localUser: User? // Your local User entity (can be null if not found or logged out)
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth
) : ViewModel() {

    // StateFlow to expose the combined user information
    private val _userInfo = MutableStateFlow<CombinedUserInfo?>(null)
    val userInfo: StateFlow<CombinedUserInfo?> = _userInfo.asStateFlow()

    // StateFlow to indicate loading status
    private val _isLoading = MutableStateFlow(true) // Start as true until initial check is done
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // AuthStateListener to react to login/logout changes
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        Log.d(TAG, "AuthState changed. Current Firebase User: ${auth.currentUser?.uid}")
        _isLoading.value = true // Set loading true while fetching local data
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // User is signed in via Firebase, fetch corresponding local user details
            fetchLocalUserData(firebaseUser)
        } else {
            // User is signed out
            _userInfo.value = null
            _isLoading.value = false
            Log.d(TAG, "User signed out. Cleared user info.")
        }
    }

    init {
        // Start listening to auth state changes when ViewModel is created
        firebaseAuth.addAuthStateListener(authStateListener)
        // Perform initial check immediately
        authStateListener.onAuthStateChanged(firebaseAuth)
    }

    // Fetch local user data based on the authenticated Firebase user
    private fun fetchLocalUserData(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            try {
                val email = firebaseUser.email
                Log.d(TAG, "Fetching local user data for email: $email")
                if (email != null) {
                    val localUser = withContext(Dispatchers.IO) {
                        userDao.getUserByEmail(email)
                    }
                    if (localUser != null) {
                        Log.d(TAG, "Local user found: ID=${localUser.id}, Name=${localUser.name}")
                        _userInfo.value = CombinedUserInfo(firebaseUser, localUser)
                    } else {
                        // Handle case where Firebase user exists but local user doesn't
                        // This might happen if local creation failed during signup or data inconsistency
                        Log.e(TAG, "Inconsistency: Firebase user exists but no local user found for email $email")
                        _userInfo.value = CombinedUserInfo(firebaseUser, null) // Provide Firebase user but null local user
                    }
                } else {
                    Log.w(TAG, "Firebase user has no email address.")
                    _userInfo.value = CombinedUserInfo(firebaseUser, null) // Cannot fetch local user without email
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching local user data", e)
                _userInfo.value = CombinedUserInfo(firebaseUser, null) // Indicate error by null local user
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Finished fetching local user data. isLoading = false")
            }
        }
    }

    // Function to handle user sign out
    fun signOut() {
        Log.d(TAG, "Signing out user...")
        firebaseAuth.signOut()
        // The AuthStateListener will automatically clear the _userInfo state
    }

    // Clean up the listener when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared. Removing AuthStateListener.")
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    // --- REMOVED Signup related states and functions ---
    // private val _username = mutableStateOf("") ...
    // private val _email = mutableStateOf("") ... // Now handled by Firebase Auth state
    // private val _password = mutableStateOf("") ...
    // private val _confirmPassword = mutableStateOf("") ...
    // private val _signupState = mutableStateOf<SignupState>(SignupState.Idle) ...
    // fun onUsernameChange(...) {}
    // fun onEmailChange(...) {}
    // fun onPasswordChange(...) {}
    // fun onConfirmPasswordChange(...) {}
    // fun handleSignup() {}
    // private fun hashPasswordPlaceholder(...) {}
    // fun resetSignupState() {}
    // --- END REMOVAL ---
}
