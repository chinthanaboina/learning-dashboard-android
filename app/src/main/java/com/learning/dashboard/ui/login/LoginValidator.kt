package com.learning.dashboard.ui.login

object LoginValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 6

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email.trim()) -> "Enter a valid email"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }
}
