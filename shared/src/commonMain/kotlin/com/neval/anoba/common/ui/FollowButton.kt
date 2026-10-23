package com.neval.anoba.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
