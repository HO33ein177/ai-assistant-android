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


// Sealed interface/class for Signup UI State
sealed interface SignupState {
    data object Idle : SignupState            // Initial state
    data object Loading : SignupState         // Signup in progress
    data class Success(val userId: Long) : SignupState // Signup successful, pass userId
    data class Error(val message: String) : SignupState // Signup failed
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    // --- UI State for Input Fields ---
    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    // --- UI State for Signup Process ---
    private val _signupState = mutableStateOf<SignupState>(SignupState.Idle)
    val signupState: State<SignupState> = _signupState

    // --- Functions to update state from UI ---
    fun onUsernameChange(value: String) {
        _username.value = value
        if (_signupState.value is SignupState.Error) _signupState.value = SignupState.Idle
    }
    fun onEmailChange(value: String) {
        _email.value = value
        if (_signupState.value is SignupState.Error) _signupState.value = SignupState.Idle
    }
    fun onPasswordChange(value: String) {
        _password.value = value
        if (_signupState.value is SignupState.Error) _signupState.value = SignupState.Idle
    }
    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
        if (_signupState.value is SignupState.Error) _signupState.value = SignupState.Idle
    }

    // --- Signup Function ---
    fun handleSignup() {
        // Reset state to Loading
        _signupState.value = SignupState.Loading
        Log.d("SignupAttempt", "Starting signup for user: ${username.value}, email: ${email.value}")

        // Basic Validation
        if (username.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            _signupState.value = SignupState.Error("Please fill all fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            _signupState.value = SignupState.Error("Invalid email address.")
            return
        }
        if (password.value != confirmPassword.value) {
            _signupState.value = SignupState.Error("Passwords do not match.")
            return
        }
        // Add more checks if needed (password length, etc.)

        viewModelScope.launch {
            try {
                // --- Check if username already exists ---
                Log.d("SignupAttempt", "Checking username uniqueness...")
                val existingUserByUsername = userDao.getUserByUsername(username.value)
                if (existingUserByUsername != null) {
                    Log.w("SignupAttempt", "Username '${username.value}' already taken.")
                    _signupState.value = SignupState.Error("Username already taken.")
                    return@launch
                }
                Log.d("SignupAttempt", "Username is unique.")

                // --- Check if email already exists ---
                Log.d("SignupAttempt", "Checking email uniqueness...")
                val existingUserByEmail = userDao.getUserByEmail(email.value) // <-- Use new DAO function
                if (existingUserByEmail != null) {
                    Log.w("SignupAttempt", "Email '${email.value}' already registered.")
                    _signupState.value = SignupState.Error("Email already registered.") // <-- Set error
                    return@launch // <-- Stop signup
                }
                Log.d("SignupAttempt", "Email is unique.")
                // --- End Email Check ---

                // Hash the password before saving (IMPORTANT SECURITY STEP)
                val hashedPassword = hashPasswordPlaceholder(password.value) // Placeholder

                val user = User(
                    name = username.value, // Assuming 'name' field in User entity
                    email = email.value,
                    password = hashedPassword // Store the hashed password
                )
                Log.d("SignupAttempt", "Inserting new user...")
                val newUserId = userDao.insert(user) // Insert returns the new user's ID

                if (newUserId > 0) {
                    Log.i("SignupAttempt", "User created successfully with ID: $newUserId")
                    _signupState.value = SignupState.Success(newUserId)
                } else {
                    Log.e("SignupAttempt", "DAO insert returned non-positive ID: $newUserId")
                    _signupState.value = SignupState.Error("Failed to create user account.")
                }

            } catch (e: Exception) {
                // Handle potential database errors
                _signupState.value = SignupState.Error("An error occurred during signup.")
                Log.e("SignupAttempt", "Database/logic error during signup", e)
            }
        }
    }

    // --- Password Hashing Placeholder ---
    private fun hashPasswordPlaceholder(password: String): String {
        // **WARNING: DO NOT USE IN PRODUCTION. IMPLEMENT REAL HASHING.**
        Log.w("UserViewModel", "Password hashing not implemented! Storing pseudo-hash.")
        return "hashed_${password}_${password.reversed()}" // Example insecure placeholder
    }

    // Function to reset state after navigation or error display
    fun resetSignupState() {
        if (_signupState.value is SignupState.Success || _signupState.value is SignupState.Error) {
            _signupState.value = SignupState.Idle
        }
    }
}
