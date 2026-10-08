package com.learning.dashboard.di

import android.content.Context
import androidx.room.Room
import com.learning.dashboard.data.auth.AuthRepository
import com.learning.dashboard.data.auth.SessionStore
import com.learning.dashboard.data.auth.SharedPrefsSessionStore
import com.learning.dashboard.data.local.LearningDatabase
import com.learning.dashboard.data.remote.FakeLearningApi
import com.learning.dashboard.data.remote.LearningApi
import com.learning.dashboard.data.remote.NetworkMonitor
import com.learning.dashboard.data.repository.CourseRepository
import com.learning.dashboard.data.repository.OfflineFirstCourseRepository
import kotlinx.serialization.json.Json

/** Manual DI: one place that wires the object graph. Hilt would replace this in a larger app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }

    private val api: LearningApi = FakeLearningApi(appContext.assets, NetworkMonitor(appContext), json)

    private val database: LearningDatabase by lazy {
        Room.databaseBuilder(appContext, LearningDatabase::class.java, "learning.db").build()
    }

    val sessionStore: SessionStore = SharedPrefsSessionStore(appContext)

    val authRepository: AuthRepository by lazy { AuthRepository(api, sessionStore) }

    val courseRepository: CourseRepository by lazy {
        OfflineFirstCourseRepository(api, database.courseDao())
    }
}
