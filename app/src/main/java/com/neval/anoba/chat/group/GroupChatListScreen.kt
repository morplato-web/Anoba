package com.neval.anoba.chat.group

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.neval.anoba.common.utils.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupChatListScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel = koinViewModel()
) {
    val groups by groupChatViewModel.groups.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val sharedGroups = remember(groups) {
        groups.map { g ->
            SharedGroupItem(
                id = g.id,
                name = g.name,
                description = g.description,
                memberCount = g.members.size,
                isPrivate = g.isPrivate
            )
        }
    }

    SharedGroupChatListScreen(
        groups = sharedGroups,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        },
        onGroupClick = { group ->
            navController.navigate("group_chat_screen/${group.id}")
        },
        onCreateGroup = { name, description, isPrivate ->
            scope.launch {
                groupChatViewModel.createGroup(name, isPrivate, description)
            }
        }
    )
}
