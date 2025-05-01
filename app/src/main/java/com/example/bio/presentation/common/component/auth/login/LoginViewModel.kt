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
import com.google.firebase.auth.FirebaseAuth // <<< Import Firebase Auth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


// Keep LoginResult sealed interface
sealed interface LoginResult {
    data object Idle : LoginResult
    data object Loading : LoginResult
    data class Success(val userId: Int) : LoginResult // Pass the LOCAL DB user ID
    data class Error(val message: String) : LoginResult
}

private const val TAG = "LoginViewModel" // Add TAG

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao // Keep UserDao to fetch local ID after Firebase login
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loginState = mutableStateOf<LoginResult>(LoginResult.Idle)
    val loginState: State<LoginResult> = _loginState

    private lateinit var firebaseAuth: FirebaseAuth // Declare FirebaseAuth instance

    init {
        firebaseAuth = FirebaseAuth.getInstance() // Initialize in init block
    }


    fun changeEmail(email: String) {
        _email.value = email
        if (_loginState.value is LoginResult.Error) _loginState.value = LoginResult.Idle
    }

    fun changePassword(password: String) {
        _password.value = password
        if (_loginState.value is LoginResult.Error) _loginState.value = LoginResult.Idle
    }

    fun attemptLogin() {
        val currentEmail = email.value.trim()
        val currentPassword = password.value

        Log.d(TAG, "Attempting login for email: $currentEmail")
        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _loginState.value = LoginResult.Error("Please enter email and password.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            _loginState.value = LoginResult.Error("Invalid email address format.")
            return
        }

        _loginState.value = LoginResult.Loading

        viewModelScope.launch {
            try {
                // 1. Sign in with Firebase Authentication
                Log.d(TAG, "Attempting Firebase sign in...")
                val authResult = firebaseAuth.signInWithEmailAndPassword(currentEmail, currentPassword).await()
                val firebaseUser = authResult.user
                Log.d(TAG, "Firebase sign in successful: UID=${firebaseUser?.uid}")

                if (firebaseUser != null) {
                    // 2. Get the corresponding LOCAL user ID from Room DB using the email
                    // (Alternatively, use firebaseUser.uid if you stored it locally and added a DAO query for it)
                    Log.d(TAG, "Fetching local user details for email: $currentEmail")
                    val localUser = withContext(Dispatchers.IO) {
                        userDao.getUserByEmail(currentEmail) // Fetch local user by email
                    }

                    if (localUser != null) {
                        Log.d(TAG, "Local user found with ID: ${localUser.id}")
                        // 3. Report Success with the LOCAL Database ID
                        _loginState.value = LoginResult.Success(localUser.id)
                    } else {
                        // This indicates an inconsistency - user exists in Firebase Auth but not locally
                        Log.e(TAG, "Login failed: User authenticated with Firebase but not found in local DB!")
                        // You might want to automatically create the local record here or show an error
                        _loginState.value = LoginResult.Error("Login failed: User data mismatch. Please try signing up again or contact support.")
                        // Optional: Sign the user out of Firebase if local record is missing
                        firebaseAuth.signOut()
                    }
                } else {
                    // Should not happen if signInWithEmailAndPassword succeeds
                    Log.e(TAG, "Firebase user was null after successful sign in task.")
                    _loginState.value = LoginResult.Error("Login failed: Could not get user details.")
                }

            } catch (e: FirebaseAuthInvalidUserException) {
                Log.w(TAG, "Login failed: User not found in Firebase Auth", e)
                _loginState.value = LoginResult.Error("Invalid email or password.") // User not found
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w(TAG, "Login failed: Invalid credentials (wrong password)", e)
                _loginState.value = LoginResult.Error("Invalid email or password.") // Wrong password
            } catch (e: Exception) { // Catch other exceptions (network, DB query, etc.)
                Log.e(TAG, "Login failed", e)
                _loginState.value = LoginResult.Error("Login failed: ${e.localizedMessage}")
            }
        }
    }

    // Remove the insecure placeholder password verification
    // private fun verifyPasswordPlaceholder(...) { ... }

    fun resetLoginState() {
        if (_loginState.value is LoginResult.Success || _loginState.value is LoginResult.Error) {
            _loginState.value = LoginResult.Idle
        }
    }
}
