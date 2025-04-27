package com.example.bio.presentation.common.component.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.UserDao
import com.example.bio.data.local.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface SignupState{
    object Idle: SignupState
    object Loading: SignupState
    data class Success(val userId: Long): SignupState
    data class Error(val message: String): SignupState
}

@HiltViewModel
class UserViewModel @Inject
constructor(private val userDao: UserDao) : ViewModel(){

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword


    fun createUser(username: String, email: String, password: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val user = User(name = username, email = email, password = password)
            val userId = userDao.insert(user)
            onSuccess(userId)
        }
    }

    fun getUserById(userId: Int, onSuccess: (User?) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            onSuccess(user)
        }
    }

    fun getUserByUsername(username: String, onSuccess: (User?) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            onSuccess(user)
        }
    }

    // --- UI State for Signup Process ---
//    private val _signupState = mutableStateOf<SignupState>(SignupState.Idle)
//    val signupState: State<SignupState> = _signupState
//
//    // --- Functions to update state from UI ---
//    fun onUsernameChange(value: String) { _username.value = value }
//    fun onEmailChange(value: String) { _email.value = value }
//    fun onPasswordChange(value: String) { _password.value = value }
//    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value }
//
//    // --- Signup Function ---
//    fun handleSignup() {
//        // Reset state to Loading
//        _signupState.value = SignupState.Loading
//
//        // Basic Validation
//        if (username.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
//            _signupState.value = SignupState.Error("Please fill all fields.")
//            return
//        }
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
//            _signupState.value = SignupState.Error("Invalid email address.")
//            return
//        }
//        if (password.value != confirmPassword.value) {
//            _signupState.value = SignupState.Error("Passwords do not match.")
//            return
//        }
//        // Add more checks if needed (password length, etc.)
//
//        viewModelScope.launch {
//            try {
//                // Check if username or email already exists (optional but recommended)
//                val existingUserByUsername = userDao.getUserByUsername(username.value)
//                if (existingUserByUsername != null) {
//                    _signupState.value = SignupState.Error("Username already taken.")
//                    return@launch
//                }
//                // You might want to add a getUserByEmail function to UserDao as well
//                // val existingUserByEmail = userDao.getUserByEmail(email.value) // Example
//                // if (existingUserByEmail != null) {
//                //     _signupState.value = SignupState.Error("Email already registered.")
//                //     return@launch
//                // }
//
//                // Hash the password before saving (IMPORTANT SECURITY STEP)
//                // You MUST implement password hashing. Storing plain text is insecure.
//                // Replace this with actual hashing (e.g., using bcrypt, Scrypt, or Argon2)
//                val hashedPassword = hashPassword(password.value) // Placeholder
//
//                val user = User(
//                    name = username.value,
//                    email = email.value,
//                    password = hashedPassword // Store the hashed password
//                )
//                val newUserId = userDao.insert(user) // Insert returns the new user's ID
//
//                if (newUserId > 0) {
//                    _signupState.value = SignupState.Success(newUserId)
//                } else {
//                    // This case might indicate an insertion error not caught by exception
//                    _signupState.value = SignupState.Error("Failed to create user account.")
//                }
//
//            } catch (e: Exception) {
//                // Handle potential database errors
//                _signupState.value = SignupState.Error("An error occurred: ${e.localizedMessage}")
//                // Log the exception e
//            }
//        }
//    }
//
//    // --- Password Hashing Placeholder ---
//    // You MUST replace this with a secure hashing implementation.
//    // Libraries like "org.mindrot:jbcrypt:0.4" or platform APIs can be used.
//    private fun hashPassword(password: String): String {
//        // **WARNING: DO NOT USE IN PRODUCTION. IMPLEMENT REAL HASHING.**
//        Log.w("UserViewModel", "Password hashing not implemented! Storing pseudo-hash.")
//        return "hashed_${password}_${password.reversed()}" // Example insecure placeholder
//    }
//
//    // Function to reset state after navigation or error display
//    fun resetSignupState() {
//        _signupState.value = SignupState.Idle
//    }
//
//    // Keep existing functions if needed
//    // fun getUserById(...)
//    // fun getUserByUsername(...)
}