package com.example.bio.presentation.common.component.auth.change_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(

) : ViewModel() {

    private val _isCodeScreen = mutableStateOf<Boolean>(true)
    val isCodeScreen: State<Boolean> = _isCodeScreen

    private val _email = mutableStateOf<String>("")
    val email: State<String> = _email

    private val _code = mutableStateOf<String>("")
    val code: State<String> = _code

    private val _rePassword = mutableStateOf<String>("")
    val rePassword: State<String> = _rePassword

    private val _password = mutableStateOf<String>("")
    val password: State<String> = _password

    fun changeIsCodeScreen(isCodePage: Boolean) {
        _isCodeScreen.value = isCodePage
    }

    fun changeEmail(email: String) {
        _email.value = email
    }

    fun changeCode(code: String) {
        _code.value = code
    }

    fun changePassword(password: String) {
        _password.value = password
    }

    fun changeRePassword(rePassword: String) {
        _rePassword.value = rePassword
    }
}