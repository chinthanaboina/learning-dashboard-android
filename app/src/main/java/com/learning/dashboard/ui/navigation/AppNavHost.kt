package com.learning.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learning.dashboard.ui.dashboard.DashboardRoute
import com.learning.dashboard.ui.detail.CourseDetailRoute
import com.learning.dashboard.ui.detail.CourseDetailViewModel
import com.learning.dashboard.ui.login.LoginRoute

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val COURSE_DETAIL = "course/{${CourseDetailViewModel.COURSE_ID_ARG}}"
    fun courseDetail(id: Int) = "course/$id"
}

@Composable
fun AppNavHost(
    isLoggedIn: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginRoute(onLoggedIn = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.DASHBOARD) {
            DashboardRoute(onCourseClick = { id -> navController.navigate(Routes.courseDetail(id)) })
        }
        composable(
            route = Routes.COURSE_DETAIL,
            arguments = listOf(navArgument(CourseDetailViewModel.COURSE_ID_ARG) { type = NavType.IntType }),
        ) {
            CourseDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
