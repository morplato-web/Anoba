package com.neval.anoba.photo.photocomment

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

class PhotoCommentViewModel(
    private val photoId: String,
    private val repository: PhotoCommentRepository,
    private val authService: AuthServiceInterface
) : ViewModel() {

    private val _comments = MutableStateFlow<List<PhotoComment>>(emptyList())
    val comments: StateFlow<List<PhotoComment>> = _comments.asStateFlow()

    // Hata mesajlarını artık Resource ID (Int) olarak tutuyoruz (Yerelleştirme için)
    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _replyingToComment = MutableStateFlow<PhotoComment?>(null)
    val replyingToComment: StateFlow<PhotoComment?> = _replyingToComment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenForComments()
    }

    private fun listenForComments() {
        viewModelScope.launch {
            repository.getCommentsStream(photoId)
                .onStart { _isLoading.value = true }
                .catch {
                    _error.value = R.string.photocomments_error_loading
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
            val comment = PhotoComment(
                photoId = photoId,
                userId = authState.uid,
                username = authState.displayName ?: "User",
                content = content,
                replyToId = _replyingToComment.value?.id,
                userProfileUrl = authState.photoUrl,
                isOfficial = authState.isOfficial,
                userBadgeType = authState.badgeType
            )

            viewModelScope.launch {
                try {
                    repository.addComment(photoId, comment)
                    _commentText.value = ""
                    _replyingToComment.value = null
                } catch (_: Exception) {
                    _error.value = R.string.photocomments_error_sending
                }
            }
        } else {
            _error.value = R.string.photocomments_error_auth_required
        }
    }

    fun onReplyClicked(comment: PhotoComment) {
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
                    repository.toggleCommentLike(photoId, commentId, authState.uid)
                } catch (_: Exception) {
                    _error.value = R.string.error_action_failed
                }
            }
        } else {
            _error.value = R.string.photocomments_error_auth_required
        }
    }

    fun deleteComment(comment: PhotoComment, userRole: String?) {
        val authState = authService.authStateFlow.value
        if (authState is AuthState.Authenticated) {
            // Admin, Muhtar veya yorum sahibi silebilir
            if (authState.uid == comment.userId || userRole == "ADMIN" || userRole == "MUHTAR") {
                viewModelScope.launch {
                    try {
                        repository.deleteComment(photoId, comment.id)
                    } catch (_: Exception) {
                        _error.value = R.string.photocomments_error_delete
                    }
                }
            } else {
                _error.value = R.string.photocomments_error_no_permission
            }
        } else {
            _error.value = R.string.photocomments_error_auth_required
        }
    }
}
