package com.learning.dashboard.data.repository

import com.learning.dashboard.domain.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    /** Emits cached courses; the local DB is the single source of truth for the UI. */
    fun observeCourses(): Flow<List<Course>>

    fun observeCourse(courseId: Int): Flow<Course?>

    /** Fetches from the network into the cache. Throws on failure; cached data is untouched. */
    suspend fun refreshCourses()

    /** Applies locally first (works offline), then syncs to the server best-effort. */
    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)
}
