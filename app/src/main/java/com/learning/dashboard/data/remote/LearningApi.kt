package com.learning.dashboard.data.remote

import kotlinx.serialization.Serializable

interface LearningApi {
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun getCourses(): List<CourseDto>
    suspend fun getLessons(courseId: Int): List<LessonDto>
    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)
}

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int,
)

@Serializable
data class LessonDto(
    val id: Int,
    val courseId: Int,
    val position: Int,
    val title: String,
    val completed: Boolean,
)

class AuthException(message: String) : Exception(message)
