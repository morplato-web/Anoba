package com.neval.anoba.chat.privatechat

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.neval.anoba.chat.privatechat.SharedChatPartner
import com.neval.anoba.chat.privatechat.SharedPrivateChatScreen
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
    val chatPartnerProfile by privateChatViewModel.chatPartnerProfile.collectAsStateWithLifecycle()
    val isPartnerTyping by privateChatViewModel.isPartnerTyping.collectAsStateWithLifecycle()
    val chatRoomStatus by privateChatViewModel.chatRoomStatus.collectAsStateWithLifecycle()
    val rawMessages by privateChatViewModel.messages.collectAsStateWithLifecycle()
    val searchResults by privateChatViewModel.searchResults.collectAsStateWithLifecycle()
    val isUploading by privateChatViewModel.isUploading.collectAsStateWithLifecycle()
    val currentUserId = privateChatViewModel.currentUserId ?: ""

    LaunchedEffect(initialTargetUserId) {
        if (!initialTargetUserId.isNullOrBlank()) {
            privateChatViewModel.fetchUserAndSetAsPartner(initialTargetUserId)
        }
    }

    val partner = remember(chatPartnerProfile) {
        chatPartnerProfile?.let { user ->
            SharedChatPartner(
                id = user.id,
                username = user.safeDisplayName,
                photoUrl = user.photoUrl,
                badgeType = user.badgeType,
                isOfficial = user.displayName?.equals("anoba", ignoreCase = true) == true
            )
        }
    }

    val sharedMessages = remember(rawMessages) {
        rawMessages.map { it.toSharedModel() }
    }

    val sharedSearchResults = remember(searchResults) {
        searchResults.map { user ->
            SharedChatPartner(
                id = user.id,
                username = user.safeDisplayName,
                photoUrl = user.photoUrl,
                badgeType = user.badgeType,
                isOfficial = user.displayName?.equals("anoba", ignoreCase = true) == true
            )
        }
    }

    SharedPrivateChatScreen(
        partner = partner,
        messages = sharedMessages,
        userSearchResults = sharedSearchResults,
        currentUserId = currentUserId,
        isLoading = isUploading,
        isPartnerTyping = isPartnerTyping,
        isPendingRequest = chatRoomStatus == "PENDING",
        onNavigateBack = {
            if (partner != null && initialTargetUserId.isNullOrBlank()) {
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
        onSelectPartner = { selected ->
            privateChatViewModel.fetchUserAndSetAsPartner(selected.id)
        },
        onSearchUser = { query ->
            privateChatViewModel.searchUsers(query)
        },
        onSendMessage = { content ->
            privateChatViewModel.sendMessage(content)
        },
        onStartVoiceCall = {
            val partnerId = partner?.id ?: ""
            if (currentUserId.isNotEmpty() && partnerId.isNotEmpty()) {
                val channelName = if (currentUserId < partnerId) "${currentUserId}_${partnerId}" else "${partnerId}_${currentUserId}"
                navController.navigate("call_screen/$channelName/false/false")
            }
        },
        onStartVideoCall = {
            val partnerId = partner?.id ?: ""
            if (currentUserId.isNotEmpty() && partnerId.isNotEmpty()) {
                val channelName = if (currentUserId < partnerId) "${currentUserId}_${partnerId}" else "${partnerId}_${currentUserId}"
                navController.navigate("call_screen/$channelName/true/false")
            }
        },
        onAcceptRequest = {
            partner?.id?.let { privateChatViewModel.acceptChatRequest(it) }
        },
        onBlockUser = { targetUid ->
            if (targetUid.isNotEmpty()) {
                privateChatViewModel.blockUser(targetUid)
                privateChatViewModel.resetChatState()
            }
        },
        onDeleteMessage = { msg ->
            val targetMsg = rawMessages.find { it.id == msg.id }
            if (targetMsg != null) {
                privateChatViewModel.onMessageLongClicked(targetMsg)
                partner?.id?.let { partnerId ->
                    privateChatViewModel.deleteSelectedMessages(partnerId)
                }
            }
        },
        onCopyMessage = { text ->
            val clipboard = navController.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("mesaj", text)
            clipboard?.setPrimaryClip(clip)
        }
    )
}
