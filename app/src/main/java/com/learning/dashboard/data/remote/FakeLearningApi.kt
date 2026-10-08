package com.learning.dashboard.data.remote

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Mock backend backed by assets/courses.json. It honours real connectivity, so turning
 * off Wi-Fi/mobile data makes calls fail exactly like a real network client would.
 */
class FakeLearningApi(
    private val assets: AssetManager,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
) : LearningApi {

    private var catalog: List<CourseDto>? = null

    override suspend fun login(email: String, password: String): LoginResponse {
        simulateNetwork()
        if (password != DEMO_PASSWORD) throw AuthException("Invalid email or password")
        return LoginResponse(token = "mock-token-${UUID.randomUUID()}")
    }

    override suspend fun getCourses(): List<CourseDto> {
        simulateNetwork()
        return loadCatalog()
    }

    override suspend fun getLessons(courseId: Int): List<LessonDto> {
        simulateNetwork()
        val course = loadCatalog().firstOrNull { it.id == courseId }
            ?: error("Course $courseId not found")
        val completedCount = (course.lessons * course.progress / 100.0).roundToInt()
        return (1..course.lessons).map { position ->
            LessonDto(
                id = courseId * 1000 + position,
                courseId = courseId,
                position = position,
                title = SAMPLE_TITLES.getOrElse(position - 1) { "Lesson $position" },
                completed = position <= completedCount,
            )
        }
    }

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) {
        simulateNetwork()
    }

    private suspend fun loadCatalog(): List<CourseDto> = catalog ?: withContext(Dispatchers.IO) {
        assets.open(COURSES_FILE).bufferedReader().use { json.decodeFromString<List<CourseDto>>(it.readText()) }
    }.also { catalog = it }

    private suspend fun simulateNetwork() {
        delay(LATENCY_MS)
        if (!networkMonitor.isOnline()) throw IOException("No internet connection")
    }

    companion object {
        const val DEMO_PASSWORD = "password123"
        private const val COURSES_FILE = "courses.json"
        private const val LATENCY_MS = 800L
        private val SAMPLE_TITLES = listOf(
            "Introduction", "Variables & Data Types", "Functions", "OOP", "Collections", "Error Handling",
        )
    }
}
