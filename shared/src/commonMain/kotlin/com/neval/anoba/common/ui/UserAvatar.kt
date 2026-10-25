package com.neval.anoba.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.chat.ui.InitialsAvatar
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@Composable
fun UserAvatar(
    photoUrl: String?,
    username: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val isAnobaBrand = username?.contains("anoba", ignoreCase = true) == true

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isAnobaBrand) AnobaGold else Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (isAnobaBrand) {
            Text(
                text = "A",
                color = AnobaNavy,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.45f).sp
            )
        } else {
            InitialsAvatar(
                name = username ?: "Anonim",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
