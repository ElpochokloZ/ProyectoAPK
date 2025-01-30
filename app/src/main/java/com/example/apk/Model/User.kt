package com.example.apk.Model

data class User (val email: String, val password: String) {

    fun isValidEmail(): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isValidPassword(): Boolean = password.length >= 6
}