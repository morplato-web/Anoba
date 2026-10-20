package com.neval.anoba.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.common.state.UiState
import com.neval.anoba.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowViewModel(
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _userListState = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val userListState: StateFlow<UiState<List<User>>> = _userListState.asStateFlow()

    fun isFollowing(userId: String): Flow<Boolean> {
        return followRepository.isFollowing(userId)
    }

    fun toggleFollow(
        userId: String, 
        isCurrentlyFollowing: Boolean,
        senderUsername: String = "",
        senderPhotoUrl: String? = null
    ) {
        viewModelScope.launch {
            try {
                if (isCurrentlyFollowing) {
                    followRepository.unfollowUser(userId)
                } else {
                    followRepository.followUser(userId, senderUsername, senderPhotoUrl)
                }
            } catch (e: Exception) {
                println("FollowViewModel: Takip işlemi hatası: ${e.message}")
            }
        }
    }
    
    fun fetchFollowers(userId: String) {
        viewModelScope.launch {
            _userListState.value = UiState.Loading
            val ids = followRepository.getFollowerIds(userId)
            val users = followRepository.getUsersDetails(ids)
            _userListState.value = UiState.Success(users)
        }
    }

    fun fetchFollowing() {
        viewModelScope.launch {
            _userListState.value = UiState.Loading
            val ids = followRepository.getFollowingIds()
            val users = followRepository.getUsersDetails(ids)
            _userListState.value = UiState.Success(users)
        }
    }

    fun clearUserList() {
        _userListState.value = UiState.Idle
    }
    
    fun syncFollows() {
        viewModelScope.launch {
            followRepository.syncFollowsFromFirebase()
        }
    }
}
