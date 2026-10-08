package com.learning.dashboard.data.repository

import com.learning.dashboard.data.local.CourseDao
import com.learning.dashboard.data.local.CourseEntity
import com.learning.dashboard.data.local.CourseWithLessons
import com.learning.dashboard.data.local.LessonEntity
import com.learning.dashboard.data.remote.CourseDto
import com.learning.dashboard.data.remote.LearningApi
import com.learning.dashboard.data.remote.LessonDto
import com.learning.dashboard.domain.Course
import com.learning.dashboard.domain.Lesson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class OfflineFirstCourseRepository(
    private val api: LearningApi,
    private val dao: CourseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCoursesWithLessons().map { list -> list.map { it.toDomain() } }

    override fun observeCourse(courseId: Int): Flow<Course?> =
        dao.observeCourseWithLessons(courseId).map { it?.toDomain() }

    override suspend fun refreshCourses(): Unit = withContext(ioDispatcher) {
        pushPendingCompletions()

        val remoteCourses = api.getCourses()
        // Prefetch lessons for every course so details also work offline.
        val remoteLessons = coroutineScope {
            remoteCourses.map { course -> async { api.getLessons(course.id) } }.awaitAll().flatten()
        }

        // Never let a refresh undo progress the user made locally.
        val localById = dao.getAllLessons().associateBy { it.id }
        dao.replaceCatalog(
            courses = remoteCourses.map { it.toEntity() },
            lessons = remoteLessons.map { remote ->
                val local = localById[remote.id]
                remote.toEntity(
                    completed = remote.completed || local?.completed == true,
                    pendingSync = local?.pendingSync == true,
                )
            },
        )
    }

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int): Unit = withContext(ioDispatcher) {
        dao.markCompletedPendingSync(lessonId) // UI updates immediately via the Room Flow
        try {
            api.markLessonCompleted(courseId, lessonId)
            dao.markSynced(lessonId)
        } catch (e: IOException) {
            // Offline: stays pendingSync and is pushed on the next successful refresh.
        }
    }

    private suspend fun pushPendingCompletions() {
        dao.getPendingLessons().forEach { lesson ->
            try {
                api.markLessonCompleted(lesson.courseId, lesson.id)
                dao.markSynced(lesson.id)
            } catch (e: IOException) {
                return // still offline; retry next time
            }
        }
    }
}

internal fun CourseWithLessons.toDomain() = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons.sortedBy { it.position }.map {
        Lesson(id = it.id, title = it.title, isCompleted = it.completed, isPendingSync = it.pendingSync)
    },
)

internal fun CourseDto.toEntity() = CourseEntity(id = id, title = title, instructor = instructor)

internal fun LessonDto.toEntity(completed: Boolean, pendingSync: Boolean) = LessonEntity(
    id = id,
    courseId = courseId,
    position = position,
    title = title,
    completed = completed,
    pendingSync = pendingSync,
)
