package com.neval.anoba.letter.lettercomment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.login.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class LetterCommentViewModel(
    private val letterId: String,
    private val repository: LetterCommentRepository,
    private val authService: AuthServiceInterface
) : ViewModel() {

    private val _comments = MutableStateFlow<List<LetterComment>>(emptyList())
    val comments: StateFlow<List<LetterComment>> = _comments.asStateFlow()

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _replyingToComment = MutableStateFlow<LetterComment?>(null)
    val replyingToComment: StateFlow<LetterComment?> = _replyingToComment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenForComments()
    }

    private fun listenForComments() {
        if (letterId.isBlank()) return

        viewModelScope.launch {
            repository.getCommentsStream(letterId)
                .onStart { _isLoading.value = true }
                .catch {
                    _error.value = R.string.error_comments_loading
                    _isLoading.value = false
                }
                .collect { commentList ->
                    _comments.value = commentList
                    _error.value = null
                    _isLoading.value = false
                }
        }
    }

    fun onCommentTextChanged(text: String) {
        _commentText.value = text
    }

    fun addComment() {
        val content = _commentText.value.trim()
        if (content.isEmpty()) return

        val authState = authService.authStateFlow.value
        if (authState is AuthState.Authenticated) {
            val comment = LetterComment(
                letterId = letterId,
                userId = authState.uid,
                username = authState.displayName ?: "User",
                content = content,
                replyToId = _replyingToComment.value?.id,
                userProfileUrl = authState.photoUrl,
                isOfficial = authState.isOfficial,
                userBadgeType = authState.badgeType
            )

            viewModelScope.launch {
                _isLoading.value = true
                try {
                    repository.addComment(letterId, comment)
                    _commentText.value = ""
                    _replyingToComment.value = null
                    _error.value = null
                } catch (_: Exception) {
                    _error.value = R.string.error_comment_send
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _error.value = R.string.error_login_required
        }
    }

    fun onReplyClicked(comment: LetterComment) {
        _replyingToComment.value = comment
    }

    fun cancelReply() {
        _replyingToComment.value = null
    }

    fun likeComment(commentId: String) {
        val authState = authService.authStateFlow.value
        if (authState is AuthState.Authenticated) {
            viewModelScope.launch {
                try {
                    repository.toggleCommentLike(letterId, commentId, authState.uid)
                } catch (_: Exception) {
                    _error.value = R.string.error_action_failed
                }
            }
        } else {
            _error.value = R.string.error_login_required
        }
    }

    fun deleteComment(comment: LetterComment) {
        val authState = authService.authStateFlow.value
        if (authState is AuthState.Authenticated && (authState.uid == comment.userId || authState.isOfficial)) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    repository.deleteComment(letterId, comment.id)
                    _error.value = null
                } catch (_: Exception) {
                    _error.value = R.string.error_delete_failed
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _error.value = R.string.error_unauthorized
        }
    }
}