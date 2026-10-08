package com.learning.dashboard.ui.common

import com.learning.dashboard.data.remote.AuthException
import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "No internet connection."
    is AuthException -> message ?: "Login failed."
    else -> "Something went wrong. Please try again."
}
