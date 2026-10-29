@file:Suppress("unused")

package com.neval.anoba.chat.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.chat.data.Messageable
import com.neval.anoba.chat.group.GroupUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseChatViewModel<M : Messageable>(
    protected open val firebaseAuth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val BASE_TAG = "BaseChatViewModel"
    }

    private val _selectedMessages = MutableStateFlow<Set<M>>(emptySet())
    val selectedMessages: StateFlow<Set<M>> = _selectedMessages.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<M?>(null)
    val replyingToMessage: StateFlow<M?> = _replyingToMessage.asStateFlow()

    val isSelectionModeActive: StateFlow<Boolean> = _selectedMessages
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    protected val currentUserDisplayName: String
        get() = firebaseAuth.currentUser?.displayName ?: firebaseAuth.currentUser?.email ?: "Siz"

    protected val currentUserPhotoUrl: String?
        get() = firebaseAuth.currentUser?.photoUrl?.toString()

    fun onMessageClicked(message: M) {
        if (isSelectionModeActive.value) {
            toggleMessageSelection(message)
        } else {
            Log.d(BASE_TAG, "Normal click on message: ${message.id} (Selection mode OFF)")
        }
    }

    fun onMessageLongClicked(message: M) {
        toggleMessageSelection(message)
        Log.d(BASE_TAG, "Long click on message: ${message.id}. Selection mode is now: ${isSelectionModeActive.value}")
    }

    private fun toggleMessageSelection(message: M) {
        viewModelScope.launch {
            val currentSelected = _selectedMessages.value.toMutableSet()
            val existing = currentSelected.find { it.id == message.id }
            if (existing != null) {
                currentSelected.remove(existing)
            } else {
                currentSelected.add(message)
            }
            _selectedMessages.value = currentSelected
        }
    }

    fun clearMessageSelection() {
        _selectedMessages.value = emptySet()
        Log.d(BASE_TAG, "Message selection cleared.")
    }

    fun setReplyingToMessage(message: M?) {
        _replyingToMessage.value = message
    }

    fun clearReplyingToMessage() {
        _replyingToMessage.value = null
    }

    open fun copySelectedMessagesToClipboard(context: Context, text: String? = null) {
        val messagesToCopy = _selectedMessages.value
        if (messagesToCopy.isNotEmpty()) {
            val textToCopy = text ?: messagesToCopy
                .sortedBy { it.timestampMillis ?: 0L }
                .joinToString("\n") { it.content ?: "" }

            GroupUtils.copyToClipboard(context, textToCopy, "Seçili Mesajlar")
            Log.d(BASE_TAG, "${messagesToCopy.size} messages copied to clipboard.")
        }
        clearMessageSelection()
    }

    abstract fun deleteSelectedMessages(chatContextId: String)
}
