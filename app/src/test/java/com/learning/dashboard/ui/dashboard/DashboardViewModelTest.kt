package com.learning.dashboard.ui.dashboard

import com.learning.dashboard.data.repository.CourseRepository
import com.learning.dashboard.domain.Course
import com.learning.dashboard.domain.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Covers the offline requirement at the decision point that matters:
 * what the user sees when the network fails, with and without cached data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `network failure with cached courses shows cached data with offline notice`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(cached = listOf(sampleCourse()), refreshError = IOException())
        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success but was $state", state is DashboardUiState.Success)
        state as DashboardUiState.Success
        assertEquals(1, state.courses.size)
        assertNotNull(state.cacheNotice)
    }

    @Test
    fun `network failure with empty cache shows error`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(cached = emptyList(), refreshError = IOException())
        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals(DashboardUiState.Error("No internet connection."), viewModel.uiState.value)
    }

    @Test
    fun `successful refresh with no courses shows empty state`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(cached = emptyList(), remote = emptyList())
        val viewModel = DashboardViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)
    }

    private fun sampleCourse() = Course(
        id = 1,
        title = "Python Programming",
        instructor = "John Smith",
        lessons = listOf(Lesson(1, "Introduction", isCompleted = true), Lesson(2, "Functions", isCompleted = false)),
    )

    private class FakeCourseRepository(
        cached: List<Course>,
        private val remote: List<Course> = cached,
        private val refreshError: Exception? = null,
    ) : CourseRepository {
        private val courses = MutableStateFlow(cached)

        override fun observeCourses(): Flow<List<Course>> = courses
        override fun observeCourse(courseId: Int): Flow<Course?> =
            courses.map { list -> list.firstOrNull { it.id == courseId } }

        override suspend fun refreshCourses() {
            refreshError?.let { throw it }
            courses.value = remote
        }

        override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) = Unit
    }
}
