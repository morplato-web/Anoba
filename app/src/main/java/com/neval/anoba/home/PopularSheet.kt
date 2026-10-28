package com.neval.anoba.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.neval.anoba.common.utils.Constants

@Composable
fun PopularSheet(
    isVisible: Boolean,
    popularItems: List<ContentItem>,
    onDismiss: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    SharedPopularSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        onItemClick = { sharedItem ->
            val route = when (sharedItem.type) {
                ContentType.PHOTO -> Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", sharedItem.id)
                ContentType.VIDEO -> Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", sharedItem.id)
                ContentType.LETTER -> Constants.LETTER_DETAIL_SCREEN.replace("{letterId}", sharedItem.id)
                ContentType.AUDIO -> Constants.SES_DETAIL_SCREEN.replace("{sesId}", sharedItem.id)
                ContentType.MARKET -> "market_detail_screen/${sharedItem.id}"
            }
            navController.navigate(route)
        },
        modifier = modifier
    )
}
