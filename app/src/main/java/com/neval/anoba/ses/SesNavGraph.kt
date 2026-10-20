package com.neval.anoba.ses

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.ses.sescomment.SesCommentsScreen

fun NavGraphBuilder.sesNavGraph(
    navController: NavController
) {
    navigation(
        startDestination = Constants.SES_HOME_SCREEN,
        route = Constants.SES_NAV_GRAPH
    ) {
        composable(Constants.SES_HOME_SCREEN) {
            SesHomeScreen(navController = navController)
        }

        composable(
            route = Constants.SES_COMMENTS_ROUTE,
            arguments = listOf(navArgument("sesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sesId = backStackEntry.arguments?.getString("sesId") ?: return@composable
            SesCommentsScreen(navController = navController, sesId = sesId)
        }

        composable(
            route = Constants.SES_DETAIL_SCREEN,
            arguments = listOf(navArgument("sesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sesId = backStackEntry.arguments?.getString("sesId")
            if (sesId != null) {
                SesDetailScreen(
                    navController = navController,
                    sesId = sesId
                )
            }
        }

        composable(
            route = Constants.SES_EDIT_SCREEN,
            arguments = listOf(navArgument("sesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sesId = backStackEntry.arguments?.getString("sesId") ?: return@composable
            SesEditScreen(
                navController = navController,
                sesId = sesId
            )
        }
    }
}
