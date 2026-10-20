package com.neval.anoba.login.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.login.viewmodel.ForgotPasswordViewModel
import com.neval.anoba.login.viewmodel.LoginViewModel
import com.neval.anoba.login.viewmodel.NewPasswordViewModel
import com.neval.anoba.login.viewmodel.SignUpViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.loginNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Constants.SPLASH_SCREEN,
        route = Constants.LOGIN_NAV_GRAPH
    ) {
        composable(Constants.SPLASH_SCREEN) {
            SplashScreen(navController = navController)
        }

        composable(Constants.LOGIN_SCREEN) {
            val viewModel: LoginViewModel = koinViewModel()
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        composable(Constants.SIGN_UP_SCREEN) {
            val viewModel: SignUpViewModel = koinViewModel()
            SignUpScreen(navController = navController, viewModel = viewModel)
        }

        composable(Constants.FORGOT_PASSWORD_SCREEN) {
            val viewModel: ForgotPasswordViewModel = koinViewModel()
            ForgotPasswordScreen(navController = navController, viewModel = viewModel)
        }

        composable(Constants.NEW_PASSWORD_SCREEN) {
            val viewModel: NewPasswordViewModel = koinViewModel()
            NewPasswordScreen(navController = navController, viewModel = viewModel)
        }
    }
}
