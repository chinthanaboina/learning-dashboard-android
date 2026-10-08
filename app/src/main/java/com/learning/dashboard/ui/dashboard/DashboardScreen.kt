package com.learning.dashboard.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learning.dashboard.domain.Course
import com.learning.dashboard.ui.AppViewModelProvider
import com.learning.dashboard.ui.common.FullScreenLoading
import com.learning.dashboard.ui.common.FullScreenMessage

@Composable
fun DashboardRoute(
    onCourseClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state = state, onRefresh = viewModel::refresh, onCourseClick = onCourseClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onCourseClick: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                actions = { TextButton(onClick = onRefresh) { Text("Refresh") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                DashboardUiState.Loading -> FullScreenLoading()
                DashboardUiState.Empty -> FullScreenMessage(
                    title = "No courses yet",
                    message = "You're not enrolled in any courses.",
                    actionLabel = "Refresh",
                    onAction = onRefresh,
                )
                is DashboardUiState.Error -> FullScreenMessage(
                    title = "Couldn't load courses",
                    message = state.message,
                    actionLabel = "Retry",
                    onAction = onRefresh,
                )
                is DashboardUiState.Success -> {
                    state.cacheNotice?.let { CachedDataBanner(it, onRefresh) }
                    if (state.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.courses, key = { it.id }) { course ->
                            CourseCard(course = course, onContinue = { onCourseClick(course.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CachedDataBanner(message: String, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$message Showing saved courses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onContinue: () -> Unit) {
    Card(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "by ${course.instructor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { course.progress / 100f }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${course.progress}% · ${course.totalLessons} lessons",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onContinue) { Text("Continue") }
            }
        }
    }
}
