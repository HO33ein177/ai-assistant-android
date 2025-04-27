package com.example.bio

sealed class Screens (val route: String) {

    object LoginScreen : Screens("login_screen")
    object ForgetPasswordScreen : Screens("forget_password_screen")

}