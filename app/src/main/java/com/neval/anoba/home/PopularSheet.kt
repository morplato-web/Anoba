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
    val sharedPopularItems = popularItems.map { item ->
        when (item) {
            is ContentItem.PhotoContent -> SharedFeedItem(
                id = item.photo.id,
                type = ContentType.PHOTO,
                title = item.photo.title ?: "",
                description = item.photo.personalNote,
                mediaUrl = item.photo.photoUrl,
                ownerId = item.photo.ownerId,
                username = item.photo.username,
                userProfileImageUrl = item.photo.userProfileImageUrl,
                userBadgeType = item.photo.userBadgeType,
                isOfficial = item.photo.isOfficial,
                reactionCount = item.photo.reactions.values.sum().toInt(),
                viewCount = item.photo.viewCount,
                createdAtMillis = item.photo.createdAt?.time ?: 0L
            )
            is ContentItem.VideoContent -> SharedFeedItem(
                id = item.video.id,
                type = ContentType.VIDEO,
                title = item.video.title ?: "",
                description = item.video.personalNote,
                mediaUrl = item.video.thumbnailUrl?.ifBlank { item.video.videoUrl } ?: item.video.videoUrl,
                ownerId = item.video.ownerId,
                username = item.video.username,
                userProfileImageUrl = item.video.userProfileImageUrl,
                userBadgeType = item.video.userBadgeType,
                isOfficial = item.video.isOfficial,
                reactionCount = item.video.reactions.values.sum().toInt(),
                viewCount = item.video.viewCount,
                createdAtMillis = item.video.createdAt?.time ?: 0L
            )
            is ContentItem.LetterContent -> SharedFeedItem(
                id = item.letter.id,
                type = ContentType.LETTER,
                title = item.letter.title ?: "",
                description = item.letter.content,
                mediaUrl = null,
                ownerId = item.letter.ownerId,
                username = item.letter.username,
                userProfileImageUrl = item.letter.userProfileImageUrl,
                userBadgeType = item.letter.userBadgeType,
                isOfficial = item.letter.isOfficial,
                reactionCount = item.letter.reactions.values.sum().toInt(),
                viewCount = item.letter.viewCount,
                createdAtMillis = item.letter.createdAt?.time ?: 0L
            )
            is ContentItem.AudioContent -> SharedFeedItem(
                id = item.audio.id,
                type = ContentType.AUDIO,
                title = item.audio.title ?: "",
                description = item.audio.personalNote,
                mediaUrl = item.audio.audioUrl,
                ownerId = item.audio.ownerId,
                username = item.audio.username,
                userProfileImageUrl = item.audio.userProfileImageUrl,
                userBadgeType = item.audio.userBadgeType,
                isOfficial = item.audio.isOfficial,
                reactionCount = item.audio.reactions.values.sum().toInt(),
                viewCount = item.audio.viewCount,
                createdAtMillis = item.audio.createdAt?.time ?: 0L
            )
            is ContentItem.MarketContent -> SharedFeedItem(
                id = item.marketItem.id,
                type = ContentType.MARKET,
                title = item.marketItem.title,
                description = item.marketItem.description,
                mediaUrl = item.marketItem.photoUrl,
                ownerId = item.marketItem.ownerId,
                username = item.marketItem.username,
                userProfileImageUrl = item.marketItem.userProfileImageUrl,
                userBadgeType = item.marketItem.userBadgeType ?: "NONE",
                isOfficial = item.marketItem.isOfficial,
                reactionCount = 0,
                viewCount = 0L,
                createdAtMillis = item.marketItem.createdAt?.time ?: 0L
            )
        }
    }

    SharedPopularSheet(
        isVisible = isVisible,
        popularItems = sharedPopularItems,
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
