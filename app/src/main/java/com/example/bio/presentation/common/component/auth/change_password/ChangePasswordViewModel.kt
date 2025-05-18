package com.example.bio.presentation.common.component.auth.change_password

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bio.data.local.dao.UserDao
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//  states for the reset process
sealed interface ResetStatus {
    data object Idle : ResetStatus            // Initial state
    data object Loading : ResetStatus         // Email sending in progress
    data object Success : ResetStatus         // Email sent successfully
    data class Error(val message: String) : ResetStatus // Failed to send email
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userDao: UserDao //  UserDao if needed for initial email check
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email


    private val _resetStatus = mutableStateOf<ResetStatus>(ResetStatus.Idle)
    val resetStatus: State<ResetStatus> = _resetStatus

    private lateinit var firebaseAuth: FirebaseAuth // Declare FirebaseAuth instance

    init {
        firebaseAuth = FirebaseAuth.getInstance() // Initialize in init block
    }

    // --- Event Handlers ---
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        // Reset status if user types again after an error/success
        if (_resetStatus.value !is ResetStatus.Idle && _resetStatus.value !is ResetStatus.Loading) {
            _resetStatus.value = ResetStatus.Idle
        }
    }

    // --- Password Reset Logic ---
    fun sendPasswordResetEmail() {
        val currentEmail = email.value.trim()
        if (currentEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            _resetStatus.value = ResetStatus.Error("Please enter a valid email address.")
            return
        }

        Log.d("ChangePasswordVM", "Attempting to send password reset email to: $currentEmail")
        _resetStatus.value = ResetStatus.Loading

        firebaseAuth.sendPasswordResetEmail(currentEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ChangePasswordVM", "Password reset email sent successfully.")
                    _resetStatus.value = ResetStatus.Success
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "An unknown error occurred."
                    Log.e("ChangePasswordVM", "Failed to send password reset email: $errorMsg", task.exception)
                    // Provide a user-friendly message
                    val userFriendlyError = when {
                        // Check for specific Firebase exceptions if needed, e.g., user not found
                        // task.exception is FirebaseAuthInvalidUserException -> "No account found with this email."
                        else -> "Failed to send reset email. Please try again."
                    }
                    _resetStatus.value = ResetStatus.Error(userFriendlyError)
                }
            }
    }

    // Function to allow UI to reset the status (e.g., after showing a success message)
    fun resetStatusHandled() {
        if (_resetStatus.value == ResetStatus.Success || _resetStatus.value is ResetStatus.Error) {
            _resetStatus.value = ResetStatus.Idle
        }
    }
}