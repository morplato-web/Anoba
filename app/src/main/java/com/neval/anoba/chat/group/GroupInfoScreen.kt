package com.neval.anoba.chat.group

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.neval.anoba.common.utils.Constants
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupInfoScreen(
    navController: NavHostController,
    groupChatViewModel: GroupChatViewModel = koinViewModel(),
    groupId: String,
    currentUserId: String?
) {
    val currentGroup by groupChatViewModel.activeGroup.collectAsStateWithLifecycle()
    val groupMembers by groupChatViewModel.groupMembers.collectAsStateWithLifecycle()
    val isMuted by groupChatViewModel.isMuted.collectAsStateWithLifecycle()

    LaunchedEffect(groupId) {
        if (groupId.isNotBlank()) {
            groupChatViewModel.setActiveGroupId(groupId)
            groupChatViewModel.loadGroupMembers(groupId)
        }
    }

    val sharedGroup = remember(currentGroup) {
        currentGroup?.let { g ->
            SharedGroupItem(
                id = g.id,
                name = g.name,
                description = g.description,
                memberCount = g.members.size,
                isPrivate = g.isPrivate
            )
        }
    }

    val sharedMembers = remember(groupMembers, currentGroup) {
        groupMembers.map { m ->
            SharedGroupMember(
                id = m.id,
                username = m.safeDisplayName,
                photoUrl = m.photoUrl,
                isOwner = currentGroup?.ownerId == m.id
            )
        }
    }

    SharedGroupInfoScreen(
        group = sharedGroup,
        members = sharedMembers,
        currentUserId = currentUserId ?: "",
        isMuted = isMuted,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        },
        onUpdateGroupInfo = { name, description ->
            groupChatViewModel.updateGroupInfo(groupId, name, description)
        },
        onToggleMute = { mute ->
            currentUserId?.let { uid ->
                if (mute) groupChatViewModel.muteGroup(groupId, uid)
                else groupChatViewModel.unmuteGroup(groupId, uid)
            }
        },
        onTogglePrivacy = { isPrivate ->
            groupChatViewModel.setGroupPrivacy(groupId, isPrivate)
        },
        onRemoveMember = { memberId ->
            groupChatViewModel.removeUserFromGroup(groupId, memberId, "")
        },
        onLeaveGroup = {
            groupChatViewModel.leaveGroup(groupId) { success ->
                if (success) navController.popBackStack()
            }
        },
        onDeleteGroup = {
            groupChatViewModel.deleteGroup(groupId)
            navController.popBackStack()
        }
    )
}
