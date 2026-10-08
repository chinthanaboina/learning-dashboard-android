package com.learning.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.learning.dashboard.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // A persisted session lets the app reopen straight into cached data while offline.
        val isLoggedIn = (application as LearningApp).container.sessionStore.getToken() != null
        setContent {
            MaterialTheme { AppNavHost(isLoggedIn = isLoggedIn) }
        }
    }
}
