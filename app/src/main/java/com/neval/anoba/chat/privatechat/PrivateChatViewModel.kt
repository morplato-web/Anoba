@file:Suppress("unused")

package com.neval.anoba.chat.privatechat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.R
import com.neval.anoba.chat.general.GeneralChatUser
import com.neval.anoba.chat.ui.BaseChatViewModel
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PrivateChatViewModel(
    private val privateChatRepository: PrivateChatRepository,
    firebaseAuth: FirebaseAuth,
    private val userRepository: IUserRepository,
    private val resourceProvider: ResourceProvider,
    private val analyticsHelper: AnalyticsHelper
) : BaseChatViewModel<PrivateMessage>(firebaseAuth) {

    companion object {
        private const val LOCAL_TAG = "PrivateChatViewModel"
    }

    private val _editingMessage = MutableStateFlow<PrivateMessage?>(null)
    val editingMessage: StateFlow<PrivateMessage?> = _editingMessage.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _chatPartnerProfile = MutableStateFlow<GeneralChatUser?>(null)
    val chatPartnerProfile: StateFlow<GeneralChatUser?> = _chatPartnerProfile.asStateFlow()

    private val _pagingMessages = MutableStateFlow<PagingData<PrivateMessage>>(PagingData.empty())
    val pagingMessages: StateFlow<PagingData<PrivateMessage>> = _pagingMessages.asStateFlow()

    private val _chatRoomStatus = MutableStateFlow<String?>(null)
    val chatRoomStatus: StateFlow<String?> = _chatRoomStatus.asStateFlow()

    private val _requestedBy = MutableStateFlow<String?>(null)
    val requestedBy: StateFlow<String?> = _requestedBy.asStateFlow()

    private val _userList = MutableStateFlow<List<GeneralChatUser>>(emptyList())
    val userList: StateFlow<List<GeneralChatUser>> = _userList.asStateFlow()
    val searchResults: StateFlow<List<GeneralChatUser>> = _userList.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    private var typingJob: Job? = null
    private var partnerTypingCollectionJob: Job? = null

    private val _blockedUsers = MutableStateFlow<List<String>>(emptyList())

    private val _undoState = MutableStateFlow<List<PrivateMessage>>(emptyList())
    val undoState: StateFlow<List<PrivateMessage>> = _undoState.asStateFlow()

    private var messageCollectionJob: Job? = null
    private var statusCollectionJob: Job? = null

    private val _currentUserBadgeType = MutableStateFlow("NONE")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val messages: StateFlow<List<PrivateMessage>> = _chatPartnerProfile
        .flatMapLatest { partner ->
            val myId = currentUserId
            if (partner == null || partner.id.isBlank() || myId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                privateChatRepository.getMessagesStream(myId, partner.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCurrentUserBadge()
        listenForCurrentUserChanges()
        setupSearch()
    }

    private fun resolveSenderName(badgeType: String?): String {
        val rawName = currentUserDisplayName.ifBlank { "Bir kullanıcı" }
        return if (badgeType == "OFFICIAL" && !rawName.contains("anoba", ignoreCase = true)) {
            "Anoba"
        } else {
            rawName
        }
    }

    private fun setupSearch() {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        userRepository.searchUsersStream(query)
                    }
                }
                .catch { e ->
                    Log.e(LOCAL_TAG, "Search error", e)
                }
                .collect { users ->
                    val currentBlocked = _blockedUsers.value
                    val filteredUsers = users.mapNotNull { user ->
                        if (user.uid == currentUserId || currentBlocked.contains(user.uid)) return@mapNotNull null
                        if (user.blockedUsers.contains(currentUserId)) return@mapNotNull null

                        GeneralChatUser(
                            id = user.uid,
                            uid = user.uid,
                            displayName = user.displayName,
                            username = user.username,
                            email = user.email,
                            photoUrl = user.photoURL,
                            role = user.role.name,
                            badgeType = user.badgeType,
                            isOfficial = user.isOfficial
                        )
                    }
                    _userList.value = filteredUsers
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun searchUsers(query: String) {
        onSearchQueryChanged(query)
    }

    private fun fetchCurrentUserBadge() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get().await()
                _currentUserBadgeType.value = userDoc.getString("badgeType") ?: "NONE"
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error fetching user badge", e)
            }
        }
    }

    private fun listenForCurrentUserChanges() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            userRepository.getAllUsersStream().collect { allUsers ->
                val me = allUsers.find { it.uid == uid }
                _blockedUsers.value = me?.blockedUsers ?: emptyList()
            }
        }
    }

    fun blockUser(targetUid: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val success = userRepository.blockUser(uid, targetUid)
            if (success) {
                analyticsHelper.logEvent("user_blocked")
                _blockedUsers.value += targetUid
                onSearchQueryChanged(_searchQuery.value)
            }
        }
    }

    fun resetChatState() {
        _chatPartnerProfile.value = null
        _messageText.value = ""
        cancelEditing()
    }

    fun updateMessageText(newText: String) {
        _messageText.value = newText
        updateTypingStatus(true)
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            updateTypingStatus(false)
        }
    }

    private fun updateTypingStatus(isTyping: Boolean) {
        val partnerId = chatPartnerProfile.value?.id ?: return
        val currentUid = currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                privateChatRepository.updateTypingStatus(currentUid, partnerId, isTyping)
            } catch (_: Exception) { }
        }
    }

    fun startEditingMessage(message: PrivateMessage) {
        _editingMessage.value = message
        _messageText.value = message.content ?: ""
    }

    fun cancelEditing() {
        _editingMessage.value = null
        _messageText.value = ""
    }

    fun applyEdit(receiverId: String) {
        val editedMessage = _editingMessage.value ?: return
        val newContent = _messageText.value.trim()
        if (newContent.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                privateChatRepository.updatePrivateMessageContent(
                    senderId = currentUserId ?: return@launch,
                    receiverId = receiverId,
                    messageId = editedMessage.id,
                    newContent = newContent
                )
                analyticsHelper.logEvent("private_message_edited")
                withContext(Dispatchers.Main) {
                    _editingMessage.value = null
                    _messageText.value = ""
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "applyEdit failed: ${e.message}")
            }
        }
    }

    fun sendMessage(content: String) {
        val partnerId = chatPartnerProfile.value?.id ?: return
        sendMessage(partnerId, content)
    }

    fun sendMessage(receiverId: String, content: String) {
        if (editingMessage.value != null) {
            applyEdit(receiverId)
            return
        }

        val currentSenderId = this.currentUserId ?: run {
            Log.e(LOCAL_TAG, "Sender ID is null. Cannot send message.")
            return
        }

        if (receiverId.isBlank() || content.trim().isEmpty()) {
            Log.d(LOCAL_TAG, "Receiver ID or message is invalid. Not sending.")
            return
        }

        val replyMessage = replyingToMessage.value

        viewModelScope.launch {
            val badge = _currentUserBadgeType.value

            val replyText = when {
                replyMessage?.content != null -> replyMessage.content
                replyMessage?.imageUrl != null -> resourceProvider.getString(R.string.chat_reply_image)
                replyMessage?.videoUrl != null -> resourceProvider.getString(R.string.chat_reply_video)
                replyMessage?.audioUrl != null -> resourceProvider.getString(R.string.chat_reply_audio)
                else -> null
            }

            val privateMessage = PrivateMessage(
                senderId = currentSenderId,
                senderName = resolveSenderName(badge),
                senderPhotoUrl = currentUserPhotoUrl,
                senderBadgeType = badge,
                receiverId = receiverId,
                receiverName = chatPartnerProfile.value?.displayName ?: resourceProvider.getString(R.string.guest),
                content = content.trim(),
                timestamp = null,
                status = "SENT",
                replyToId = replyMessage?.id,
                replyToName = replyMessage?.senderName,
                replyToContent = replyText
            )

            try {
                privateChatRepository.sendPrivateMessage(privateMessage)
                analyticsHelper.logEvent("private_message_sent") {
                    putString("type", "text")
                }
                _messageText.value = ""
                clearReplyingToMessage()
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error sending message via repository", e)
            }
        }
    }

    fun sendImageMessage(receiverId: String, uri: Uri, messageText: String) {
        val currentSenderId = this.currentUserId ?: return
        val chatRoomId = if (currentSenderId < receiverId) "${currentSenderId}_${receiverId}" else "${receiverId}_${currentSenderId}"

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val badge = _currentUserBadgeType.value

                val imageUrl = privateChatRepository.uploadImage(chatRoomId, uri)
                if (imageUrl != null) {
                    val message = PrivateMessage(
                        senderId = currentSenderId,
                        senderName = resolveSenderName(badge),
                        senderPhotoUrl = currentUserPhotoUrl,
                        senderBadgeType = badge,
                        receiverId = receiverId,
                        receiverName = chatPartnerProfile.value?.displayName ?: resourceProvider.getString(R.string.guest),
                        content = messageText.trim().ifEmpty { null },
                        imageUrl = imageUrl,
                        timestamp = null
                    )
                    privateChatRepository.sendPrivateMessage(message, resourceProvider.getString(R.string.chathome_photo))
                    analyticsHelper.logEvent("private_message_sent") {
                        putString("type", "image")
                    }
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendVideoMessage(receiverId: String, uri: Uri, messageText: String) {
        val currentSenderId = this.currentUserId ?: return
        val chatRoomId = if (currentSenderId < receiverId) "${currentSenderId}_${receiverId}" else "${receiverId}_${currentSenderId}"

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val badge = _currentUserBadgeType.value

                val videoUrl = privateChatRepository.uploadVideo(chatRoomId, uri)
                if (videoUrl != null) {
                    val message = PrivateMessage(
                        senderId = currentSenderId,
                        senderName = resolveSenderName(badge),
                        senderPhotoUrl = currentUserPhotoUrl,
                        senderBadgeType = badge,
                        receiverId = receiverId,
                        receiverName = chatPartnerProfile.value?.displayName ?: resourceProvider.getString(R.string.guest),
                        content = messageText.trim().ifEmpty { null },
                        videoUrl = videoUrl,
                        timestamp = null
                    )
                    privateChatRepository.sendPrivateMessage(message, resourceProvider.getString(R.string.chathome_video))
                    analyticsHelper.logEvent("private_message_sent") {
                        putString("type", "video")
                    }
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendAudioMessage(receiverId: String, audioFile: File, duration: Long) {
        val currentSenderId = this.currentUserId ?: return
        val chatRoomId = if (currentSenderId < receiverId) "${currentSenderId}_${receiverId}" else "${receiverId}_${currentSenderId}"

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val badge = _currentUserBadgeType.value

                val audioUrl = privateChatRepository.uploadAudio(chatRoomId, Uri.fromFile(audioFile))
                if (audioUrl != null) {
                    val message = PrivateMessage(
                        senderId = currentSenderId,
                        senderName = resolveSenderName(badge),
                        senderPhotoUrl = currentUserPhotoUrl,
                        senderBadgeType = badge,
                        receiverId = receiverId,
                        receiverName = chatPartnerProfile.value?.displayName ?: resourceProvider.getString(R.string.guest),
                        audioUrl = audioUrl,
                        audioDuration = duration,
                        timestamp = null
                    )
                    privateChatRepository.sendPrivateMessage(message, resourceProvider.getString(R.string.mycontent_new_audio))
                    analyticsHelper.logEvent("private_message_sent") {
                        putString("type", "audio")
                        putLong("duration", duration)
                    }
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun listenForMessages(otherUserId: String) {
        val currentUserId = this.currentUserId
        if (currentUserId.isNullOrBlank() || otherUserId.isBlank()) return

        val previousPartnerId = chatPartnerProfile.value?.id
        if (previousPartnerId != null && previousPartnerId != otherUserId) {
            privateChatRepository.stopRealtimeSync(currentUserId, previousPartnerId)
        }

        messageCollectionJob?.cancel()
        statusCollectionJob?.cancel()
        partnerTypingCollectionJob?.cancel()

        loadChatPartnerProfile(otherUserId)

        statusCollectionJob = viewModelScope.launch {
            privateChatRepository.getChatRoomStatusStream(currentUserId, otherUserId).collect { (status, requestedBy) ->
                _chatRoomStatus.value = status
                _requestedBy.value = requestedBy
            }
        }

        partnerTypingCollectionJob = viewModelScope.launch {
            privateChatRepository.getTypingStatusStream(currentUserId, otherUserId).collect { isTyping ->
                _isPartnerTyping.value = isTyping
            }
        }

        messageCollectionJob = viewModelScope.launch {
            privateChatRepository.getMessagesPaging(currentUserId, otherUserId)
                .cachedIn(viewModelScope)
                .collect { _pagingMessages.value = it }
        }
    }

    fun fetchUserAndSetAsPartner(userId: String) {
        listenForMessages(userId)
    }

    override fun onCleared() {
        super.onCleared()
        val currentPartnerId = chatPartnerProfile.value?.id
        val currentUid = currentUserId
        if (currentUid != null && currentPartnerId != null) {
            updateTypingStatus(false)
            privateChatRepository.stopRealtimeSync(currentUid, currentPartnerId)
        }
        typingJob?.cancel()
        messageCollectionJob?.cancel()
        statusCollectionJob?.cancel()
        partnerTypingCollectionJob?.cancel()
    }

    override fun deleteSelectedMessages(chatContextId: String) {
        val currentUserId = this.currentUserId ?: return
        val messagesToDelete = selectedMessages.value

        if (chatContextId.isBlank()) {
            clearMessageSelection()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val deletedMessages = mutableListOf<PrivateMessage>()
            messagesToDelete.forEach { message ->
                if (message.senderId == currentUserId) {
                    try {
                        privateChatRepository.deletePrivateMessage(currentUserId, chatContextId, message)
                        deletedMessages.add(message)
                        analyticsHelper.logEvent("private_message_deleted")
                    } catch (e: Exception) {
                        Log.e(LOCAL_TAG, "Error deleting message", e)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _undoState.value = deletedMessages
                clearMessageSelection()
            }
        }
    }

    fun clearUndoState() {
        _undoState.value = emptyList()
    }

    fun undoDelete(partnerId: String) {
        val currentUserId = this.currentUserId ?: return
        if (partnerId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                privateChatRepository.addPrivateMessages(currentUserId, partnerId, _undoState.value)
                withContext(Dispatchers.Main) {
                    _undoState.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error undoing delete", e)
            }
        }
    }

    fun acceptChatRequest(partnerId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                privateChatRepository.acceptChatRequest(uid, partnerId)
                _chatRoomStatus.value = "ACCEPTED"
                analyticsHelper.logEvent("chat_request_accepted")
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error accepting chat request", e)
            }
        }
    }

    fun markMessagesAsRead(messageIds: List<String>) {
        val uid = currentUserId ?: return
        val partnerId = _chatPartnerProfile.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                privateChatRepository.markMessagesAsRead(uid, partnerId, messageIds)
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error marking messages as read", e)
            }
        }
    }

    fun loadMoreHistory(otherUserId: String, anchorTimestamp: Long? = null) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            privateChatRepository.loadOlderMessages(uid, otherUserId, anchorTimestamp)
        }
    }

    fun fetchUserAndSetAsPartner(userId: String, onComplete: (GeneralChatUser?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    val chatUser = GeneralChatUser(
                        id = user.uid,
                        uid = user.uid,
                        displayName = user.displayName,
                        username = user.username,
                        email = user.email,
                        photoUrl = user.photoURL,
                        role = user.role.name,
                        badgeType = user.badgeType,
                        isOfficial = user.isOfficial
                    )
                    withContext(Dispatchers.Main) {
                        onComplete(chatUser)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error fetching user by ID", e)
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    private fun loadChatPartnerProfile(partnerUserId: String) {
        if (partnerUserId.isBlank()) {
            _chatPartnerProfile.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userRepository.getUserById(partnerUserId)
                if (user == null) {
                    withContext(Dispatchers.Main) { _chatPartnerProfile.value = null }
                    return@launch
                }

                val chatUser = GeneralChatUser(
                    id = user.uid,
                    uid = user.uid,
                    displayName = user.displayName,
                    username = user.username,
                    email = user.email,
                    photoUrl = user.photoURL,
                    role = user.role.name,
                    badgeType = user.badgeType,
                    isOfficial = user.isOfficial
                )

                withContext(Dispatchers.Main) {
                    _chatPartnerProfile.value = chatUser
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _chatPartnerProfile.value = null
                }
            }
        }
    }
}
