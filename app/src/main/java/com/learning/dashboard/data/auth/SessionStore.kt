package com.learning.dashboard.data.auth

import android.content.Context

interface SessionStore {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clear()
}

/**
 * Demo implementation only. In production the token would be encrypted with an
 * Android Keystore-backed key before being persisted (see README, Security).
 * Callers depend on [SessionStore], so swapping the implementation touches one class.
 */
class SharedPrefsSessionStore(context: Context) : SessionStore {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    override fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    override fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TOKEN = "auth_token"
    }
}
