package com.neval.anoba.chat.privatechat

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.neval.anoba.chat.ui.SharedChatPartner
import com.neval.anoba.chat.ui.SharedPrivateChatScreen
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivateChatScreen(
    navController: NavHostController,
    privateChatViewModel: PrivateChatViewModel,
    initialTargetUserId: String? = null,
    @Suppress("UNUSED_PARAMETER") authViewModel: AuthViewModel = koinViewModel()
) {
    val chatPartnerProfile by privateChatViewModel.chatPartnerProfile.collectAsState()
    val isPartnerTyping by privateChatViewModel.isPartnerTyping.collectAsState()
    val chatRoomStatus by privateChatViewModel.chatRoomStatus.collectAsState()

    var partner by remember(chatPartnerProfile) {
        mutableStateOf(
            chatPartnerProfile?.let { user ->
                SharedChatPartner(
                    id = user.id,
                    username = user.safeDisplayName,
                    photoUrl = user.photoUrl,
                    badgeType = null,
                    isOfficial = user.displayName?.equals("anoba", ignoreCase = true) == true
                )
            }
        )
    }

    LaunchedEffect(initialTargetUserId) {
        if (!initialTargetUserId.isNullOrBlank()) {
            privateChatViewModel.fetchUserAndSetAsPartner(initialTargetUserId) { user ->
                if (user != null) {
                    partner = SharedChatPartner(
                        id = user.id,
                        username = user.safeDisplayName,
                        photoUrl = user.photoUrl,
                        badgeType = null,
                        isOfficial = user.displayName?.equals("anoba", ignoreCase = true) == true
                    )
                }
            }
        }
    }

    SharedPrivateChatScreen(
        partner = partner,
        isPartnerTyping = isPartnerTyping,
        isPendingRequest = chatRoomStatus == "PENDING",
        onNavigateBack = {
            if (partner != null && initialTargetUserId.isNullOrBlank()) {
                partner = null
                privateChatViewModel.resetChatState()
            } else {
                navController.popBackStack()
            }
        },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        },
        onStartVoiceCall = {
            val currentId = privateChatViewModel.currentUserId ?: ""
            val partnerId = partner?.id ?: ""
            if (currentId.isNotEmpty() && partnerId.isNotEmpty()) {
                val channelName = if (currentId < partnerId) "${currentId}_${partnerId}" else "${partnerId}_${currentId}"
                navController.navigate("call_screen/$channelName/false/false")
            }
        },
        onStartVideoCall = {
            val currentId = privateChatViewModel.currentUserId ?: ""
            val partnerId = partner?.id ?: ""
            if (currentId.isNotEmpty() && partnerId.isNotEmpty()) {
                val channelName = if (currentId < partnerId) "${currentId}_${partnerId}" else "${partnerId}_${currentId}"
                navController.navigate("call_screen/$channelName/true/false")
            }
        },
        onAcceptRequest = {
            partner?.id?.let { privateChatViewModel.acceptChatRequest(it) }
        },
        onBlockUser = { targetUid ->
            if (targetUid.isNotEmpty()) {
                privateChatViewModel.blockUser(targetUid)
                partner = null
                privateChatViewModel.resetChatState()
            }
        }
    )
}
