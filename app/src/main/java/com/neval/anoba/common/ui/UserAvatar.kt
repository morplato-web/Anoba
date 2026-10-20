package com.neval.anoba.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.ui.InitialsAvatar

@Composable
fun UserAvatar(
    photoUrl: String?,
    username: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    imageLoader: ImageLoader? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                imageLoader = imageLoader ?: coil3.compose.LocalPlatformContext.current.let { coil3.SingletonImageLoader.get(it) },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_default_profile)
            )
        } else {
            InitialsAvatar(
                name = username ?: "Anonim",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
