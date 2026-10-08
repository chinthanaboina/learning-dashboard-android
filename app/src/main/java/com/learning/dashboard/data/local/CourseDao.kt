package com.learning.dashboard.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {

    @Transaction
    @Query("SELECT * FROM courses ORDER BY id")
    abstract fun observeCoursesWithLessons(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    abstract fun observeCourseWithLessons(courseId: Int): Flow<CourseWithLessons?>

    @Query("SELECT * FROM lessons")
    abstract suspend fun getAllLessons(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE pendingSync = 1")
    abstract suspend fun getPendingLessons(): List<LessonEntity>

    // @Upsert (not REPLACE): REPLACE deletes the row first, which would cascade-delete lessons.
    @Upsert
    abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    abstract suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    abstract suspend fun deleteCoursesNotIn(keepIds: List<Int>)

    @Query("UPDATE lessons SET completed = 1, pendingSync = 1 WHERE id = :lessonId")
    abstract suspend fun markCompletedPendingSync(lessonId: Int)

    @Query("UPDATE lessons SET pendingSync = 0 WHERE id = :lessonId")
    abstract suspend fun markSynced(lessonId: Int)

    /** Atomically replaces the cached catalog so observers never see a half-written state. */
    @Transaction
    open suspend fun replaceCatalog(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        deleteCoursesNotIn(courses.map { it.id })
        upsertCourses(courses)
        upsertLessons(lessons)
    }
}
