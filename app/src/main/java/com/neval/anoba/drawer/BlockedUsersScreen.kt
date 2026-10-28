package com.neval.anoba.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.profile.BlockedUserItem
import com.neval.anoba.profile.SharedBlockedUsersScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlockedUsersScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val blockedUsers by authViewModel.blockedUsers.collectAsState()

    SharedBlockedUsersScreen(
        blockedUsers = blockedUsers.map { user ->
            BlockedUserItem(
                id = user.id,
                displayName = user.safeDisplayName,
                photoUrl = user.photoUrl
            )
        },
        onNavigateBack = { navController.popBackStack() },
        onUnblockUser = { userId -> authViewModel.unblockUser(userId) }
    )
}
