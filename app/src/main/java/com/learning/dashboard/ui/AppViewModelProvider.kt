package com.learning.dashboard.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.learning.dashboard.LearningApp
import com.learning.dashboard.di.AppContainer
import com.learning.dashboard.ui.dashboard.DashboardViewModel
import com.learning.dashboard.ui.detail.CourseDetailViewModel
import com.learning.dashboard.ui.login.LoginViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(container().authRepository) }
        initializer { DashboardViewModel(container().courseRepository) }
        initializer { CourseDetailViewModel(createSavedStateHandle(), container().courseRepository) }
    }

    private fun CreationExtras.container(): AppContainer =
        (checkNotNull(this[APPLICATION_KEY]) as LearningApp).container
}
