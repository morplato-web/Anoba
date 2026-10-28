package com.neval.anoba.chat.group

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupChatScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel,
    groupId: String,
    @Suppress("UNUSED_PARAMETER") authViewModel: AuthViewModel = koinViewModel()
) {
    val activeGroup by groupChatViewModel.activeGroup.collectAsStateWithLifecycle()
    val isUploading by groupChatViewModel.isUploading.collectAsStateWithLifecycle()
    val rawMessages by groupChatViewModel.messages.collectAsStateWithLifecycle()
    val currentUserId = groupChatViewModel.currentUserId ?: ""

    LaunchedEffect(groupId) {
        if (groupId.isNotBlank()) {
            groupChatViewModel.setActiveGroupId(groupId)
        }
    }

    val sharedGroup = remember(activeGroup) {
        activeGroup?.toSharedModel()
    }

    val sharedMessages = remember(rawMessages) {
        rawMessages.map { it.toSharedModel() }
    }

    val typingUsers by remember(activeGroup) {
        derivedStateOf {
            activeGroup?.typingUsers?.filterKeys { it != currentUserId }?.values?.toList() ?: emptyList()
        }
    }

    SharedGroupChatScreen(
        group = sharedGroup,
        messages = sharedMessages,
        currentUserId = currentUserId,
        isLoading = isUploading,
        typingUsers = typingUsers,
        onNavigateBack = {
            groupChatViewModel.setActiveGroupId(null)
            navController.popBackStack()
        },
        onHomeClick = {
            groupChatViewModel.setActiveGroupId(null)
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        },
        onGroupInfoClick = { id ->
            navController.navigate("${Constants.GROUP_INFO_SCREEN}/$id")
        },
        onSendMessage = { content ->
            groupChatViewModel.addMessageToCurrentGroup(content, null)
        }
    )
}
