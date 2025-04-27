package com.example.bio.presentation.common.component.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bio.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val userDao: UserDao)
 : ViewModel() {

    private val _email = mutableStateOf<String>("")
    val email: State<String> = _email

    private val _password = mutableStateOf<String>("")
    val password: State<String> = _password

    private val _loginState = mutableStateOf<LoginResult>(LoginResult.Idle)
    val loginState: State<LoginResult> = _loginState

    fun changeEmail(email: String) {
        _email.value = email
    }

    fun changePassword(password: String) {
        _password.value = password
    }

    fun attemptLogin() {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val user = userDao.getUserByUsername(email.value) // Or by email
                if (user != null /* && checkPassword(password.value, user.passwordHash) */ ) {
                    // Password check needs implementation (NEVER store plain text passwords)
                    _loginState.value = LoginResult.Success(user.id)
                } else {
                    _loginState.value = LoginResult.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("An error occurred: ${e.localizedMessage}")
            }
        }
    }


    // Define LoginResult sealed class/interface elsewhere
    sealed interface LoginResult {
        data object Idle : LoginResult
        data object Loading : LoginResult
        data class Success(val userId: Int) : LoginResult
        data class Error(val message: String) : LoginResult
    }

}