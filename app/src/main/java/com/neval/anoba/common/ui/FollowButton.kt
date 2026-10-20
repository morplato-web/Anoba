package com.neval.anoba.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.R
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.follow.FollowViewModel
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import org.koin.androidx.compose.koinViewModel

@Composable
fun FollowButton(
    targetUserId: String,
    currentUserId: String?,
    modifier: Modifier = Modifier,
    followViewModel: FollowViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    isDarkBackground: Boolean = false
) {
    if (currentUserId == null || currentUserId == targetUserId) return

    val isFollowing by followViewModel.isFollowing(targetUserId).collectAsState(initial = false)
    val authState by authViewModel.authState.collectAsState()
    
    val senderName = (authState as? AuthState.Authenticated)?.displayName ?: ""
    val senderPhoto = (authState as? AuthState.Authenticated)?.photoUrl

    val backgroundColor = when {
        !isFollowing -> AnobaGold
        isDarkBackground -> Color.White.copy(alpha = 0.2f)
        else -> AnobaNavy.copy(alpha = 0.1f)
    }

    val textColor = when {
        !isFollowing -> Color.White
        isDarkBackground -> Color.White
        else -> AnobaNavy
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { 
                followViewModel.toggleFollow(
                    userId = targetUserId, 
                    isCurrentlyFollowing = isFollowing,
                    senderUsername = senderName,
                    senderPhotoUrl = senderPhoto
                ) 
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isFollowing) stringResource(id = R.string.following) else stringResource(id = R.string.follow),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
