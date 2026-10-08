package com.learning.dashboard.domain

import kotlin.math.roundToInt

data class Lesson(
    val id: Int,
    val title: String,
    val isCompleted: Boolean,
    val isPendingSync: Boolean = false,
)

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>,
) {
    val totalLessons: Int get() = lessons.size
    val completedLessons: Int get() = lessons.count { it.isCompleted }

    /** Progress is derived from lesson state, so it can never drift from what the user sees. */
    val progress: Int get() = ProgressCalculator.percentage(completedLessons, totalLessons)
}

object ProgressCalculator {
    fun percentage(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        return (completed * 100.0 / total).roundToInt().coerceIn(0, 100)
    }
}
