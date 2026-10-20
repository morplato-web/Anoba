package com.neval.anoba.chat.general

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.R
import com.neval.anoba.chat.ui.BaseChatViewModel
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class GeneralChatViewModel(
    private val generalChatRepository: GeneralChatRepository,
    firebaseAuth: FirebaseAuth,
    private val analyticsHelper: AnalyticsHelper,
    private val resourceProvider: ResourceProvider,
) : BaseChatViewModel<GeneralChatMessage>(firebaseAuth) {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingMessages: Flow<PagingData<GeneralChatMessage>> = _searchQuery
        .flatMapLatest { query ->
            generalChatRepository.getMessagesPaging("GeneralChat", query)
        }
        .cachedIn(viewModelScope)

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private fun resolveSenderName(badgeType: String?): String {
        val rawName = currentUserDisplayName.ifBlank { "Bir kullanıcı" }
        return if (badgeType == "OFFICIAL" && !rawName.contains("anoba", ignoreCase = true)) {
            "Anoba"
        } else {
            rawName
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendMessage(chatRoomId: String, content: String, photoUrl: String?, badgeType: String?) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return
        val replyMessage = replyingToMessage.value

        viewModelScope.launch {
            val replyText = when {
                replyMessage?.content != null -> replyMessage.content
                replyMessage?.imageUrl != null -> resourceProvider.getString(R.string.chat_reply_image)
                replyMessage?.videoUrl != null -> resourceProvider.getString(R.string.chat_reply_video)
                replyMessage?.audioUrl != null -> resourceProvider.getString(R.string.chat_reply_audio)
                else -> null
            }

            val chatMessage = GeneralChatMessage(
                senderId = currentUserId ?: "",
                senderName = resolveSenderName(badgeType),
                content = trimmed,
                senderPhotoUrl = photoUrl,
                senderBadgeType = badgeType,
                replyToId = replyMessage?.id,
                replyToName = replyMessage?.senderName,
                replyToContent = replyText
            )
            addMessage(chatRoomId, chatMessage)
            clearReplyingToMessage()
        }
    }

    private fun addMessage(chatRoomId: String, message: GeneralChatMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                generalChatRepository.addMessage(chatRoomId, message)
                analyticsHelper.logEvent("general_chat_message_sent") {
                    putString("type", "text")
                }
            } catch (e: Exception) {
                Log.e("GeneralChatViewModel", "Message could not be sent", e)
            }
        }
    }

    fun editMessage(chatRoomId: String, messageId: String, newContent: String) {
        val trimmed = newContent.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            generalChatRepository.editMessage(chatRoomId, messageId, trimmed)
            analyticsHelper.logEvent("general_chat_message_edited")
        }
    }

    override fun deleteSelectedMessages(chatContextId: String) {
        deleteMessages(chatContextId, selectedMessages.value.toList())
        clearMessageSelection()
    }

    private fun deleteMessages(chatRoomId: String, messages: List<GeneralChatMessage>) {
        viewModelScope.launch(Dispatchers.IO) {
            messages.forEach { message ->
                generalChatRepository.deleteMessage(chatRoomId, message)
                analyticsHelper.logEvent("general_chat_message_deleted")
            }
        }
    }

    fun sendImageMessage(chatRoomId: String, uri: Uri, message: String, photoUrl: String?, badgeType: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            try {
                val imageUrl = generalChatRepository.uploadImage(chatRoomId, uri)
                if (imageUrl != null) {
                    val chatMessage = GeneralChatMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        content = message.trim().ifEmpty { null },
                        imageUrl = imageUrl,
                        senderPhotoUrl = photoUrl,
                        senderBadgeType = badgeType
                    )
                    addMessage(chatRoomId, chatMessage)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendVideoMessage(chatRoomId: String, uri: Uri, message: String, photoUrl: String?, badgeType: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            try {
                val videoUrl = generalChatRepository.uploadVideo(chatRoomId, uri)
                if (videoUrl != null) {
                    val chatMessage = GeneralChatMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        content = message.trim().ifEmpty { null },
                        videoUrl = videoUrl,
                        senderPhotoUrl = photoUrl,
                        senderBadgeType = badgeType
                    )
                    addMessage(chatRoomId, chatMessage)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendAudioMessage(chatRoomId: String, audioFile: File, duration: Long, photoUrl: String?, badgeType: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            try {
                val audioUrl = generalChatRepository.uploadAudio(chatRoomId, Uri.fromFile(audioFile))
                if (audioUrl != null) {
                    val chatMessage = GeneralChatMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        audioUrl = audioUrl,
                        audioDuration = duration,
                        senderPhotoUrl = photoUrl,
                        senderBadgeType = badgeType
                    )
                    addMessage(chatRoomId, chatMessage)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        generalChatRepository.stopRealtimeSync()
    }
}