package com.example.bio.presentation.common.component.auth.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


// Define LoginResult sealed interface (can be in a separate file too)
sealed interface LoginResult {
    data object Idle : LoginResult            // Initial state
    data object Loading : LoginResult         // Login in progress
    data class Success(val userId: Int) : LoginResult // Login successful, pass userId
    data class Error(val message: String) : LoginResult // Login failed
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao // Injected via Hilt
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loginState = mutableStateOf<LoginResult>(LoginResult.Idle)
    val loginState: State<LoginResult> = _loginState

    fun changeEmail(email: String) {
        _email.value = email
        // Reset error when user types
        if (_loginState.value is LoginResult.Error) _loginState.value = LoginResult.Idle
    }

    fun changePassword(password: String) {
        _password.value = password
        // Reset error when user types
        if (_loginState.value is LoginResult.Error) _loginState.value = LoginResult.Idle
    }

    fun attemptLogin() {
        // --- Basic Input Validation ---
        Log.d("LoginAttempt", "Attempting login for email: ${email.value}") // Log attempt start
        if (email.value.isBlank() || password.value.isBlank()) {
            _loginState.value = LoginResult.Error("Please enter email and password.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            _loginState.value = LoginResult.Error("Invalid email address format.")
            return
        }
        // --- End Validation ---

        _loginState.value = LoginResult.Loading // Set state to Loading

        viewModelScope.launch {
            try {
                // Fetch user by email (adjust DAO method if needed)
                Log.d("LoginAttempt", "Querying DAO for user...")
                val user = userDao.getUserByEmail(email.value) // Assuming this fetches by email for now

                if (user == null) {
                    Log.d("LoginViewModel", "User not found for email: ${email.value}")
                    _loginState.value = LoginResult.Error("Invalid email or password.")
                    return@launch
                }
                else {
                    Log.d("LoginAttempt", "User FOUND: ID=${user.id}, Email=${user.email}, StoredHash=${user.password}")
                }


                // --- Password Verification ---
                // Replace placeholder with actual secure check
                Log.d("LoginAttempt", "Verifying password...")
                if (verifyPasswordPlaceholder(password.value, user.password)) {
                    // Password matches
                    Log.d("LoginViewModel", "Password verified for user: ${user.id}")
                    _loginState.value = LoginResult.Success(user.id) // Success state with user ID
                } else {
                    // Password does not match
                    Log.d("LoginViewModel", "Password mismatch for user: ${user.id}")
                    _loginState.value = LoginResult.Error("Invalid email or password.")
                }

            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("An error occurred during login.")
                Log.e("LoginAttempt", "Login database/logic error", e) // Log the actual error
            }
        }
    }

    // --- Password Verification Placeholder ---
    private fun verifyPasswordPlaceholder(plainPassword: String, storedHash: String): Boolean {
        // **WARNING: REPLACE THIS WITH SECURE PASSWORD VERIFICATION (e.g., BCrypt.checkpw)**
        Log.w("LoginViewModel", "Using insecure placeholder password verification!")
        val expectedHash = "hashed_${plainPassword}_${plainPassword.reversed()}" // Matches insecure signup hash
        val match = storedHash == expectedHash
        Log.d("LoginViewModel", "Password verification result: $match (Plain: '$plainPassword', Stored Hash: '$storedHash', Expected Placeholder Hash: '$expectedHash')")
        Log.d("VerifyPassword", "Plain: '$plainPassword', Stored: '$storedHash', Expected: '$expectedHash', Match: $match")
        return match
    }

    // Function to reset state after navigation or error display
    fun resetLoginState() {
        if (_loginState.value is LoginResult.Success || _loginState.value is LoginResult.Error) {
            _loginState.value = LoginResult.Idle
        }
    }
}
