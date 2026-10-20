package com.neval.anoba.video.videocomment

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

class VideoCommentViewModel(
    private val videoId: String,
    private val repository: VideoCommentRepository,
    private val authService: AuthServiceInterface
) : ViewModel() {
    private val _comments = MutableStateFlow<List<VideoComment>>(emptyList())
    val comments: StateFlow<List<VideoComment>> = _comments.asStateFlow()

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error.asStateFlow()
    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _replyingToComment = MutableStateFlow<VideoComment?>(null)
    val replyingToComment: StateFlow<VideoComment?> = _replyingToComment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenForComments()
    }
    private fun listenForComments() {
        viewModelScope.launch {
            repository.getCommentsStream(videoId)
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
            val comment = VideoComment(
                videoId = videoId,
                userId = authState.uid,
                username = authState.displayName ?: "",
                content = content,
                replyToId = _replyingToComment.value?.id,
                userProfileUrl = authState.photoUrl,
                isOfficial = authState.isOfficial,
                userBadgeType = authState.badgeType
            )

            viewModelScope.launch {
                try {
                    repository.addComment(videoId, comment)
                    _commentText.value = ""
                    _replyingToComment.value = null
                } catch (_: Exception) {
                    _error.value = R.string.error_comment_send
                }
            }
        } else {
            _error.value = R.string.error_login_required
        }
    }

    fun onReplyClicked(comment: VideoComment) {
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
                    repository.toggleCommentLike(videoId, commentId, authState.uid)
                } catch (_: Exception) {
                    _error.value = R.string.error_action_failed
                }
            }
        } else {
            _error.value = R.string.error_login_required
        }
    }

    fun deleteComment(comment: VideoComment) {
        val authState = authService.authStateFlow.value
        if (authState is AuthState.Authenticated && authState.uid == comment.userId) {
            viewModelScope.launch {
                try {
                    repository.deleteComment(videoId, comment.id)
                } catch (_: Exception) {
                    _error.value = R.string.error_delete_failed
                }
            }
        } else {
            _error.value = R.string.error_unauthorized
        }
    }
}
