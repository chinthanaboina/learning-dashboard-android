package com.learning.dashboard.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learning.dashboard.domain.Course
import com.learning.dashboard.domain.Lesson
import com.learning.dashboard.ui.AppViewModelProvider
import com.learning.dashboard.ui.common.FullScreenLoading
import com.learning.dashboard.ui.common.FullScreenMessage

@Composable
fun CourseDetailRoute(
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    CourseDetailScreen(state, snackbarHostState, onBack, viewModel::onMarkCompleted)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    state: CourseDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onMarkCompleted: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (state as? CourseDetailUiState.Success)?.course?.title ?: "Course",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                CourseDetailUiState.Loading -> FullScreenLoading()
                CourseDetailUiState.NotFound -> FullScreenMessage(
                    title = "Course not found",
                    message = "This course isn't available offline yet. Go back and refresh.",
                )
                is CourseDetailUiState.Success -> CourseDetailContent(state.course, onMarkCompleted)
            }
        }
    }
}

@Composable
private fun CourseDetailContent(course: Course, onMarkCompleted: (Int) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 8.dp)) {
                Text(course.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("${course.progress}% complete", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { course.progress / 100f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(
                    "${course.completedLessons} of ${course.totalLessons} lessons completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(course.lessons, key = { it.id }) { lesson ->
            LessonRow(lesson = lesson, onMarkCompleted = { onMarkCompleted(lesson.id) })
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onMarkCompleted: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (lesson.isCompleted) "✓ Completed" else "○ Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lesson.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (lesson.isPendingSync) {
                    Text(
                        "Saved on device · will sync when online",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!lesson.isCompleted) {
                OutlinedButton(onClick = onMarkCompleted) { Text("Mark complete") }
            }
        }
    }
}
