package com.neval.anoba.photo

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.photo.photocomment.PhotoCommentsScreen

fun NavGraphBuilder.photoNavGraph(navController: NavController) {
    navigation(
        startDestination = Constants.PHOTO_HOME_SCREEN,
        route = Constants.PHOTO_NAV_GRAPH
    ) {
        composable(Constants.PHOTO_HOME_SCREEN) {
            PhotoHomeScreen(navController = navController)
        }
        composable(
            route = Constants.PHOTO_DETAIL_SCREEN,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            PhotoDetailScreen(navController = navController, photoId = photoId)
        }
        composable(
            route = Constants.PHOTO_EDIT_SCREEN,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: return@composable
            PhotoEditScreen(navController = navController, photoId = photoId)
        }
        composable(
            route = Constants.PHOTO_COMMENTS_ROUTE,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            PhotoCommentsScreen(navController = navController, photoId = photoId)
        }
    }
}