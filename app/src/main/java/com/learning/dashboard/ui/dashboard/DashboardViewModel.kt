package com.learning.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.dashboard.data.repository.CourseRepository
import com.learning.dashboard.domain.Course
import com.learning.dashboard.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean = false,
        /** Non-null when the last refresh failed and cached data is being shown. */
        val cacheNotice: String? = null,
    ) : DashboardUiState
}

class DashboardViewModel(private val repository: CourseRepository) : ViewModel() {

    private sealed interface RefreshState {
        data object Idle : RefreshState
        data object InProgress : RefreshState
        data class Failed(val message: String) : RefreshState
    }

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.InProgress)

    val uiState: StateFlow<DashboardUiState> =
        combine(repository.observeCourses(), refreshState) { courses, refresh -> toUiState(courses, refresh) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState.InProgress
            refreshState.value = try {
                repository.refreshCourses()
                RefreshState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RefreshState.Failed(e.toUserMessage())
            }
        }
    }

    private fun toUiState(courses: List<Course>, refresh: RefreshState): DashboardUiState = when {
        courses.isNotEmpty() -> DashboardUiState.Success(
            courses = courses,
            isRefreshing = refresh is RefreshState.InProgress,
            cacheNotice = (refresh as? RefreshState.Failed)?.message,
        )
        refresh is RefreshState.InProgress -> DashboardUiState.Loading
        refresh is RefreshState.Failed -> DashboardUiState.Error(refresh.message)
        else -> DashboardUiState.Empty
    }
}
