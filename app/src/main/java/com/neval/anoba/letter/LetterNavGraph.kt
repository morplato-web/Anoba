package com.neval.anoba.letter

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.letter.lettercomment.LetterCommentsScreen

fun NavGraphBuilder.letterNavGraph(navController: NavController) {
    navigation(
        startDestination = Constants.LETTER_HOME_SCREEN,
        route = Constants.LETTER_NAV_GRAPH
    ) {
        composable(route = Constants.LETTER_HOME_SCREEN) {
            LetterHomeScreen(
                navController = navController
            )
        }

        composable(route = Constants.LETTER_CREATE_SCREEN) {
            LetterCreateScreen(navController = navController)
        }

        composable(
            route = Constants.LETTER_DETAIL_SCREEN,
            arguments = listOf(navArgument("letterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val letterId = backStackEntry.arguments?.getString("letterId") ?: ""
            LetterDetailScreen(
                letterId = letterId,
                navController = navController
            )
        }

        // YENİ EKLENDİ
        composable(
            route = Constants.LETTER_EDIT_SCREEN,
            arguments = listOf(navArgument("letterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val letterId = backStackEntry.arguments?.getString("letterId") ?: ""
            LetterEditScreen(
                letterId = letterId,
                navController = navController
            )
        }

        composable(
            route = Constants.LETTER_COMMENTS_ROUTE,
            arguments = listOf(navArgument("letterId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val letterId = backStackEntry.arguments?.getString("letterId") ?: ""
            LetterCommentsScreen(
                navController = navController,
                letterId = letterId,
            )
        }

        composable(route = Constants.LETTER_ADMIN_SCREEN) {
            LetterAdminScreen(navController = navController)
        }
    }
}
