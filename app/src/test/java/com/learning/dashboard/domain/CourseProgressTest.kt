package com.learning.dashboard.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseProgressTest {

    @Test
    fun `completing a lesson updates course progress`() {
        val course = Course(
            id = 1, title = "Python", instructor = "John Smith",
            lessons = listOf(
                Lesson(1, "Introduction", isCompleted = true),
                Lesson(2, "Variables", isCompleted = true),
                Lesson(3, "Functions", isCompleted = false),
                Lesson(4, "OOP", isCompleted = false),
            ),
        )
        assertEquals(50, course.progress)

        val updated = course.copy(lessons = course.lessons.map { if (it.id == 3) it.copy(isCompleted = true) else it })
        assertEquals(75, updated.progress)
    }

    @Test
    fun `course without lessons has zero progress instead of dividing by zero`() {
        assertEquals(0, ProgressCalculator.percentage(completed = 0, total = 0))
    }
}
