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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.follow.FollowViewModel
import org.koin.compose.koinInject

@Composable
fun FollowButton(
    isFollowing: Boolean,
    onToggleFollow: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isFollowing) Color(0xFF334155) else Color(0xFFFFD700)
    val textColor = if (isFollowing) Color.White else Color.Black

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onToggleFollow(isFollowing) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FollowButton(
    targetUserId: String,
    currentUserId: String?,
    modifier: Modifier = Modifier,
    followViewModel: FollowViewModel = koinInject(),
    isDarkBackground: Boolean = false
) {
    if (currentUserId == null || currentUserId == targetUserId) return

    val isFollowing by followViewModel.isFollowing(targetUserId).collectAsState(initial = false)

    val backgroundColor = when {
        !isFollowing -> Color(0xFFFFD700)
        isDarkBackground -> Color.White.copy(alpha = 0.2f)
        else -> Color(0xFF232066).copy(alpha = 0.1f)
    }

    val textColor = when {
        !isFollowing -> Color.White
        isDarkBackground -> Color.White
        else -> Color(0xFF232066)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { 
                followViewModel.toggleFollow(
                    userId = targetUserId, 
                    isCurrentlyFollowing = isFollowing,
                    senderUsername = "",
                    senderPhotoUrl = null
                ) 
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
