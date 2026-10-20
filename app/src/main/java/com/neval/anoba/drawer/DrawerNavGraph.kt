package com.neval.anoba.drawer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.home.HomeScreen

import androidx.navigation.NavType
import androidx.navigation.navArgument

fun NavGraphBuilder.drawerNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Constants.HOME_SCREEN,
        route = Constants.DRAWER_GRAPH
    ) {
        composable(
            route = Constants.HOME_SCREEN + "?countryCode={countryCode}",
            arguments = listOf(
                navArgument("countryCode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode")
            HomeScreen(
                navController = navController,
                countryCode = countryCode
            )
        }
        composable(Constants.PROFILE_SCREEN) {
            ProfileScreen(navController)
        }
        composable(Constants.PROFILE_EDIT_SCREEN) {
            ProfileEditScreen(navController)
        }
        composable(Constants.BLOCKED_USERS_SCREEN) {
            BlockedUsersScreen(navController)
        }
    }
}
