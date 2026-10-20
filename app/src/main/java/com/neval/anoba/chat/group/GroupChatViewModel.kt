package com.neval.anoba.chat.group

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.R
import com.neval.anoba.chat.general.GeneralChatUser
import com.neval.anoba.chat.ui.BaseChatViewModel
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.utils.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GroupChatViewModel(
    private val userRepository: IUserRepository,
    private val groupChatRepository: GroupChatRepository,
    private val resourceProvider: ResourceProvider,
    override val firebaseAuth: FirebaseAuth
) : BaseChatViewModel<GroupMessage>(firebaseAuth) {

    companion object {
        private const val LOCAL_TAG = "GroupChatViewModel"
    }

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _pagingMessages = MutableStateFlow<PagingData<GroupMessage>>(PagingData.empty())
    val pagingMessages: StateFlow<PagingData<GroupMessage>> = _pagingMessages.asStateFlow()

    private val _groups = MutableStateFlow<List<ChatGroup>>(emptyList())
    val groups: StateFlow<List<ChatGroup>> = _groups.asStateFlow()

    private val _activeGroup = MutableStateFlow<ChatGroup?>(null)
    val activeGroup: StateFlow<ChatGroup?> = _activeGroup.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private var typingJob: Job? = null
    private var messageCollectionJob: Job? = null

    private val _groupMembers = MutableStateFlow<List<GeneralChatUser>>(emptyList())
    val groupMembers: StateFlow<List<GeneralChatUser>> = _groupMembers.asStateFlow()

    private val _allUsers = MutableStateFlow<List<GeneralChatUser>>(emptyList())
    val allUsers: StateFlow<List<GeneralChatUser>> = _allUsers.asStateFlow()

    private val _undoState = MutableStateFlow<List<GroupMessage>>(emptyList())
    val undoState: StateFlow<List<GroupMessage>> = _undoState.asStateFlow()

    init {
        currentUserId?.let { listenForGroups(it) }
        loadAllUsers()
    }

    private fun resolveSenderName(badgeType: String?): String {
        val rawName = currentUserDisplayName.ifBlank { "Bir üye" }
        return if (badgeType == "OFFICIAL" && !rawName.contains("anoba", ignoreCase = true)) {
            "Anoba"
        } else {
            rawName
        }
    }

    private fun listenForGroups(userId: String) {
        viewModelScope.launch {
            groupChatRepository.getGroupsStreamForUser(userId).catch { exception ->
                Log.e(LOCAL_TAG, "Error listening for groups", exception)
            }.collect { groupList ->
                _groups.value = groupList
            }
        }
    }

    fun setActiveGroupId(groupId: String?) {
        val oldGroupId = _activeGroup.value?.id
        if (oldGroupId != null && oldGroupId != groupId) {
            groupChatRepository.stopRealtimeSync(oldGroupId)
        }

        messageCollectionJob?.cancel()

        if (groupId == null) {
            _activeGroup.value = null
            _groupMembers.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val foundGroup = groups.value.find { it.id == groupId } ?: groupChatRepository.getGroupDetails(groupId)

            withContext(Dispatchers.Main) {
                _activeGroup.value = foundGroup
            }

            if (foundGroup != null) {
                loadGroupMembers(foundGroup.id)

                messageCollectionJob = viewModelScope.launch {
                    groupChatRepository.getMessagesPaging(foundGroup.id)
                        .cachedIn(viewModelScope)
                        .collect { _pagingMessages.value = it }
                }
            }
        }
    }

    suspend fun createGroup(name: String, isPrivate: Boolean, description: String): String? {
        val ownerId = currentUserId ?: return null
        val newGroup = ChatGroup(
            name = name,
            description = description,
            ownerId = ownerId,
            isPrivate = isPrivate,
            members = listOf(ownerId)
        )
        return try {
            val createdGroup = groupChatRepository.createGroup(newGroup)
            listenForGroups(ownerId)
            createdGroup.id
        } catch (e: Exception) {
            Log.e(LOCAL_TAG, "Error creating group", e)
            _errorMessage.value = resourceProvider.getString(R.string.chat_group_create_error, e.localizedMessage ?: "")
            null
        }
    }

    fun addMessageToCurrentGroup(content: String, badgeType: String?) {
        val groupId = activeGroup.value?.id ?: return
        val replyMessage = replyingToMessage.value

        viewModelScope.launch {
            val replyText = when {
                replyMessage?.content != null -> replyMessage.content
                replyMessage?.imageUrl != null -> resourceProvider.getString(R.string.chat_reply_image)
                replyMessage?.videoUrl != null -> resourceProvider.getString(R.string.chat_reply_video)
                replyMessage?.audioUrl != null -> resourceProvider.getString(R.string.chat_reply_audio)
                else -> null
            }

            val message = GroupMessage(
                senderId = currentUserId ?: "",
                senderName = resolveSenderName(badgeType),
                senderPhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString(),
                senderBadgeType = badgeType,
                content = content,
                groupId = groupId,
                replyToId = replyMessage?.id,
                replyToName = replyMessage?.senderName,
                replyToContent = replyText
            )
            addMessage(groupId, message)
            clearReplyingToMessage()
        }
    }

    fun addMessage(groupId: String, message: GroupMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupChatRepository.addMessageToGroup(groupId, message)
                withContext(Dispatchers.Main) {
                    if (message.content != null) {
                        _messageText.value = ""
                    }
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error sending group message", e)
            }
        }
    }

    fun sendImageMessage(groupId: String, uri: Uri, badgeType: String?) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val imageUrl = groupChatRepository.uploadImage(groupId, uri)
                if (imageUrl != null) {
                    val message = GroupMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        senderPhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString(),
                        senderBadgeType = badgeType,
                        imageUrl = imageUrl,
                        groupId = groupId
                    )
                    addMessage(groupId, message)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendVideoMessage(groupId: String, uri: Uri, badgeType: String?) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val videoUrl = groupChatRepository.uploadVideo(groupId, uri)
                if (videoUrl != null) {
                    val message = GroupMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        senderPhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString(),
                        senderBadgeType = badgeType,
                        videoUrl = videoUrl,
                        groupId = groupId
                    )
                    addMessage(groupId, message)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun sendAudioMessage(groupId: String, audioFile: File, duration: Long, badgeType: String?) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val audioUrl = groupChatRepository.uploadAudio(groupId, Uri.fromFile(audioFile))
                if (audioUrl != null) {
                    val message = GroupMessage(
                        senderId = currentUserId ?: "",
                        senderName = resolveSenderName(badgeType),
                        senderPhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString(),
                        senderBadgeType = badgeType,
                        audioUrl = audioUrl,
                        audioDuration = duration,
                        groupId = groupId
                    )
                    addMessage(groupId, message)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        val groupId = activeGroup.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupChatRepository.editMessageContent(groupId, messageId, newContent)
                withContext(Dispatchers.Main) {
                    updateMessageText("")
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error editing group message", e)
            }
        }
    }

    override fun deleteSelectedMessages(chatContextId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messagesToDelete = selectedMessages.value.toList()
                val deletedMessages = mutableListOf<GroupMessage>()
                messagesToDelete.forEach { message ->
                    groupChatRepository.deleteMessageFromGroup(chatContextId, message)
                    deletedMessages.add(message)
                }
                withContext(Dispatchers.Main) {
                    _undoState.value = deletedMessages
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error deleting group messages", e)
            } finally {
                withContext(Dispatchers.Main) {
                    clearMessageSelection()
                }
            }
        }
    }

    fun clearUndoState() {
        _undoState.value = emptyList()
    }

    fun undoDelete(groupId: String) {
        if (groupId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupChatRepository.addMessagesToGroup(groupId, _undoState.value)
                withContext(Dispatchers.Main) {
                    _undoState.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error undoing group message delete", e)
            }
        }
    }

    fun updateGroupImage(groupId: String, imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = groupChatRepository.uploadGroupImage(groupId, imageUri)
                groupChatRepository.updateGroupImageUrl(groupId, imageUrl)
                val currentGroup = _activeGroup.value
                if (currentGroup != null && currentGroup.id == groupId) {
                    _activeGroup.value = currentGroup.copy(imageUrl = imageUrl)
                }
            } catch (e: Exception) {
                _errorMessage.value = resourceProvider.getString(R.string.chat_group_image_update_failed)
                Log.e(LOCAL_TAG, "Failed to update group image", e)
            }
        }
    }

    fun updateMessageText(newText: String) {
        _messageText.value = newText
        updateTypingStatus(true)
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            updateTypingStatus(false)
        }
    }

    fun onMessageSent() {
        typingJob?.cancel()
        updateTypingStatus(false)
    }

    fun loadMoreHistory(anchorTimestamp: Long? = null) {
        val groupId = _activeGroup.value?.id ?: return
        viewModelScope.launch {
            groupChatRepository.loadOlderMessages(groupId, anchorTimestamp)
        }
    }

    private fun updateTypingStatus(isTyping: Boolean) {
        val groupId = activeGroup.value?.id ?: return
        val userId = currentUserId ?: return
        val userName = currentUserDisplayName.ifBlank { "Bir üye" }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupChatRepository.updateTypingStatus(groupId, userId, userName, isTyping)
            } catch (_: Exception) { }
        }
    }

    fun markMessagesAsRead(messageIds: List<String>) {
        if (messageIds.isEmpty()) return
        val groupId = activeGroup.value?.id ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.markMessagesAsReadBatch(groupId, messageIds, userId)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.deleteGroup(groupId)
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val group = _activeGroup.value
                if (group != null && group.id == groupId) {
                    val members = groupChatRepository.getGroupMemberDetails(group.members)
                    withContext(Dispatchers.Main) {
                        _groupMembers.value = members
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsersStream()
                .catch { _ ->
                    _errorMessage.value = resourceProvider.getString(R.string.mycontent_error_loading)
                }
                .collect { usersFromRepo ->
                    val chatUsers = usersFromRepo.map { user ->
                        val finalId = user.uid.ifBlank { user.documentId }.ifBlank { "generated_${UUID.randomUUID()}" }
                        GeneralChatUser(
                            id = finalId,
                            uid = user.uid.ifBlank { user.documentId },
                            displayName = user.displayName,
                            username = user.username,
                            email = user.email,
                            photoUrl = user.photoURL,
                            role = user.role.name,
                            badgeType = user.badgeType,
                            isOfficial = user.isOfficial
                        )
                    }
                    _allUsers.value = chatUsers
                }
        }
    }

    fun inviteUserToGroup(groupId: String, userIdToInvite: String, userNameToInvite: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentGroup = _activeGroup.value
            if (currentGroup != null && currentGroup.members.contains(userIdToInvite)) {
                Log.d(LOCAL_TAG, "Kullanıcı zaten grupta ekli: $userIdToInvite")
                return@launch
            }
            if (currentGroup != null) {
                val updatedMembers = currentGroup.members + userIdToInvite
                _activeGroup.value = currentGroup.copy(members = updatedMembers)
            }
            groupChatRepository.addUserToGroup(groupId, userIdToInvite)
            loadGroupMembers(groupId)

            val systemMessage = GroupMessage(
                senderId = "SYSTEM",
                senderName = "SYSTEM",
                content = resourceProvider.getString(R.string.chat_group_user_added, userNameToInvite),
                groupId = groupId,
                senderPhotoUrl = null
            )
            groupChatRepository.addMessageToGroup(groupId, systemMessage)
        }
    }

    fun removeUserFromGroup(groupId: String, userIdToRemove: String, userNameToRemove: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentGroup = _activeGroup.value
            if (currentGroup != null) {
                val updatedMembers = currentGroup.members.filter { it != userIdToRemove }
                _activeGroup.value = currentGroup.copy(members = updatedMembers)
            }
            groupChatRepository.removeUserFromGroup(groupId, userIdToRemove)
            loadGroupMembers(groupId)

            val systemMessage = GroupMessage(
                senderId = "SYSTEM",
                senderName = "SYSTEM",
                content = resourceProvider.getString(R.string.chat_group_user_removed, userNameToRemove),
                groupId = groupId,
                senderPhotoUrl = null
            )
            groupChatRepository.addMessageToGroup(groupId, systemMessage)
        }
    }

    fun leaveGroup(groupId: String, onResult: (Boolean) -> Unit) {
        val userId = currentUserId ?: return
        val userName = currentUserDisplayName.ifBlank { "Bir üye" }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupChatRepository.removeUserFromGroup(groupId, userId)

                val systemMessage = GroupMessage(
                    senderId = userId,
                    senderName = userName,
                    content = resourceProvider.getString(R.string.chat_group_user_left),
                    groupId = groupId,
                    senderPhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString()
                )
                groupChatRepository.addMessageToGroup(groupId, systemMessage)

                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e(LOCAL_TAG, "Error leaving group", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun setGroupPrivacy(groupId: String, isPrivate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.setGroupPrivacy(groupId, isPrivate)
        }
    }

    fun updateGroupInfo(groupId: String, newName: String, newDescription: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.updateGroupInfo(groupId, newName, newDescription)
        }
    }

    fun muteGroup(groupId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.muteGroup(groupId, userId)
            _isMuted.value = true
        }
    }

    fun unmuteGroup(groupId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.unmuteGroup(groupId, userId)
            _isMuted.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        typingJob?.cancel()
        messageCollectionJob?.cancel()
        _activeGroup.value?.id?.let { groupChatRepository.stopRealtimeSync(it) }
    }
}