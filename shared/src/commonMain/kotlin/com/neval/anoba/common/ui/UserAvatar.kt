package com.neval.anoba.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.neval.anoba.chat.ui.InitialsAvatar

@Composable
fun UserAvatar(
    photoUrl: String?,
    username: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    imageLoader: ImageLoader? = null
) {
    val cleanUrl = photoUrl?.trim()
    val isAnobaBrand = username?.contains("anoba", ignoreCase = true) == true
    val fallbackName = username?.ifBlank { "Anonim" } ?: "Anonim"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9)),
        contentAlignment = Alignment.Center
    ) {
        if (!cleanUrl.isNullOrBlank()) {
            if (imageLoader != null) {
                AsyncImage(
                    model = cleanUrl,
                    contentDescription = fallbackName,
                    imageLoader = imageLoader,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = cleanUrl,
                    contentDescription = fallbackName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else if (isAnobaBrand) {
            AnobaBrandLogoIcon(
                modifier = Modifier.fillMaxSize(),
                size = size
            )
        } else {
            InitialsAvatar(
                name = fallbackName,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}