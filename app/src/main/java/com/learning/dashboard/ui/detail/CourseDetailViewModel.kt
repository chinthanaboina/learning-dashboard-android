package com.learning.dashboard.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.dashboard.data.repository.CourseRepository
import com.learning.dashboard.domain.Course
import com.learning.dashboard.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState
    data object NotFound : CourseDetailUiState
    data class Success(val course: Course) : CourseDetailUiState
}

class CourseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CourseRepository,
) : ViewModel() {

    private val courseId: Int = checkNotNull(savedStateHandle[COURSE_ID_ARG])

    val uiState: StateFlow<CourseDetailUiState> = repository.observeCourse(courseId)
        .map<Course?, CourseDetailUiState> { course ->
            if (course == null) CourseDetailUiState.NotFound else CourseDetailUiState.Success(course)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState.Loading)

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-off messages (snackbars) that must not be replayed on rotation. */
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onMarkCompleted(lessonId: Int) {
        viewModelScope.launch {
            try {
                repository.markLessonCompleted(courseId, lessonId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.toUserMessage())
            }
        }
    }

    companion object {
        const val COURSE_ID_ARG = "courseId"
    }
}
