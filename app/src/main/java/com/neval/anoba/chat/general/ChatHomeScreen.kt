package com.neval.anoba.chat.general

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.neval.anoba.chat.ui.SharedChatHomeScreen
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel

@Composable
fun ChatHomeScreen(
    navController: NavHostController,
    @Suppress("UNUSED_PARAMETER") chatViewModel: GeneralChatViewModel,
    @Suppress("UNUSED_PARAMETER") authViewModel: AuthViewModel
) {
    SharedChatHomeScreen(
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        },
        onOpenGroupsClick = { navController.navigate(Constants.GROUP_CHAT_LIST_SCREEN) },
        onOpenPrivateChatClick = { navController.navigate(Constants.PRIVATE_CHAT_SCREEN) }
    )
}
