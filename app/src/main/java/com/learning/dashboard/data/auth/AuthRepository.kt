package com.learning.dashboard.data.auth

import com.learning.dashboard.data.remote.LearningApi

class AuthRepository(
    private val api: LearningApi,
    private val sessionStore: SessionStore,
) {
    suspend fun login(email: String, password: String) {
        val response = api.login(email, password)
        sessionStore.saveToken(response.token)
    }
}
