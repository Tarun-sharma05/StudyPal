package com.example.studypal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studypal.ui.screens.HomeScreen
import com.example.studypal.ui.screens.LoginScreen
import com.example.studypal.ui.screens.RegisterScreen
import com.example.studypal.ui.screens.RevisionPlanScreen
import com.example.studypal.ui.screens.SubjectInputScreen
import com.example.studypal.ui.screens.ProfileScreen
import com.example.studypal.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object SubjectInput : Screen("subject_input")
    object RevisionPlan : Screen("revision_plan")
    object Home : Screen("home")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.user != null) Screen.Home.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.SubjectInput.route) {
            SubjectInputScreen(navController = navController)
        }
        
        composable(Screen.RevisionPlan.route) {
            RevisionPlanScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
} 