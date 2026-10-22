package com.neval.anoba.market

import androidx.navigation.NavController
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
                onCreateItemClick = { navController.navigate(Constants.MARKET_CREATE_SCREEN) },
                onItemClick = { itemId ->
                    navController.navigate("market_detail_screen/$itemId")
                },
                onSendMessageClick = { ownerId, username ->
                    navController.navigate("${Constants.PRIVATE_CHAT_SCREEN}/$ownerId?sellerName=$username")
                }
            )
        }

        composable(Constants.MARKET_CREATE_SCREEN) {
            MarketCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Constants.MARKET_DETAIL_SCREEN) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            MarketDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onSendMessageClick = { ownerId, username ->
                    navController.navigate("${Constants.PRIVATE_CHAT_SCREEN}/$ownerId?sellerName=$username")
                }
            )
        }
    }
}
