package com.neval.anoba.market

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.neval.anoba.common.utils.Constants

fun NavGraphBuilder.marketNavGraph(navController: NavController) {
    navigation(
        startDestination = Constants.MARKET_HOME_SCREEN,
        route = Constants.MARKET_NAV_GRAPH
    ) {
        composable(Constants.MARKET_HOME_SCREEN) {
            MarketHomeScreen(
                onNavigateBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate(Constants.HOME_SCREEN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCreateItemClick = { navController.navigate(Constants.MARKET_CREATE_SCREEN) },
                onItemClick = { itemId ->
                    navController.navigate("market_detail_screen/$itemId")
                }
            )
        }

        composable(Constants.MARKET_CREATE_SCREEN) {
            MarketCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                viewModel = org.koin.androidx.compose.koinViewModel()
            )
        }

        composable(Constants.MARKET_DETAIL_SCREEN) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            MarketDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate(Constants.HOME_SCREEN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onEditClick = { id ->
                    navController.navigate("market_edit_screen/$id")
                },
                onSellerClick = { userId ->
                    navController.navigate("my_content_screen?userId=$userId")
                },
                onSendMessageClick = { ownerId, _ ->
                    navController.navigate("${Constants.PRIVATE_CHAT_SCREEN}?userId=$ownerId")
                }
            )
        }

        composable(Constants.MARKET_EDIT_SCREEN) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            MarketEditScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
