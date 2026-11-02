package com.neval.anoba.home

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SharedHomeRepository {

    private val firestore = Firebase.firestore

    private inline fun <reified T> getField(doc: DocumentSnapshot, field: String): T? {
        return try {
            doc.get<T>(field)
        } catch (_: Exception) {
            null
        }
    }

    fun getCombinedHomeFeed(): Flow<List<SharedFeedItem>> {
        val lettersFlow: Flow<List<SharedFeedItem>> = firestore.collection("letters")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val isPublished = getField<Boolean>(doc, "is_published") ?: true
                        val isFlagged = getField<Boolean>(doc, "is_flagged") ?: false
                        if (!isPublished || isFlagged) return@mapNotNull null

                        val text = getField<String>(doc, "text") ?: getField<String>(doc, "personalNote") ?: ""
                        val title = getField<String>(doc, "title") ?: text.take(30)
                        val ownerId = getField<String>(doc, "ownerId") ?: ""
                        val username = getField<String>(doc, "username") ?: "Anonim"
                        val userProfileImageUrl = getField<String>(doc, "userProfileImageUrl")
                        val isOfficial = getField<Boolean>(doc, "isOfficial") ?: false
                        val badgeType = getField<String>(doc, "userBadgeType") ?: "NONE"
                        val reactionCount = getField<Int>(doc, "reactionCount") ?: 0
                        val viewCount = getField<Long>(doc, "viewCount") ?: 0L
                        val createdAt = getField<Long>(doc, "createdAt") ?: getField<Long>(doc, "createdAtMillis") ?: 0L

                        SharedFeedItem(
                            id = doc.id,
                            type = ContentType.LETTER,
                            title = title,
                            description = text,
                            ownerId = ownerId,
                            username = username,
                            userProfileImageUrl = userProfileImageUrl,
                            userBadgeType = badgeType,
                            isOfficial = isOfficial,
                            topEmoji = "❤️",
                            reactionCount = reactionCount,
                            viewCount = viewCount,
                            createdAtMillis = createdAt
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { emit(emptyList()) }

        val photosFlow: Flow<List<SharedFeedItem>> = firestore.collection("photos")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val isPublished = getField<Boolean>(doc, "is_published") ?: true
                        val isFlagged = getField<Boolean>(doc, "is_flagged") ?: false
                        if (!isPublished || isFlagged) return@mapNotNull null

                        val url = getField<String>(doc, "photoUrl") ?: getField<String>(doc, "url") ?: ""
                        val title = getField<String>(doc, "title") ?: "Fotoğraf"
                        val note = getField<String>(doc, "personalNote") ?: ""
                        val ownerId = getField<String>(doc, "ownerId") ?: ""
                        val username = getField<String>(doc, "username") ?: "Anonim"
                        val userProfileImageUrl = getField<String>(doc, "userProfileImageUrl")
                        val isOfficial = getField<Boolean>(doc, "isOfficial") ?: false
                        val badgeType = getField<String>(doc, "userBadgeType") ?: "NONE"
                        val reactionCount = getField<Int>(doc, "reactionCount") ?: 0
                        val viewCount = getField<Long>(doc, "viewCount") ?: 0L
                        val createdAt = getField<Long>(doc, "createdAt") ?: getField<Long>(doc, "createdAtMillis") ?: 0L

                        SharedFeedItem(
                            id = doc.id,
                            type = ContentType.PHOTO,
                            title = title,
                            description = note,
                            mediaUrl = url,
                            ownerId = ownerId,
                            username = username,
                            userProfileImageUrl = userProfileImageUrl,
                            userBadgeType = badgeType,
                            isOfficial = isOfficial,
                            topEmoji = "🔥",
                            reactionCount = reactionCount,
                            viewCount = viewCount,
                            createdAtMillis = createdAt
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { emit(emptyList()) }

        val videosFlow: Flow<List<SharedFeedItem>> = firestore.collection("videos")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val isPublished = getField<Boolean>(doc, "is_published") ?: true
                        val isFlagged = getField<Boolean>(doc, "is_flagged") ?: false
                        if (!isPublished || isFlagged) return@mapNotNull null

                        val url = getField<String>(doc, "videoUrl") ?: ""
                        val title = getField<String>(doc, "title") ?: "Video"
                        val note = getField<String>(doc, "personalNote") ?: ""
                        val ownerId = getField<String>(doc, "ownerId") ?: ""
                        val username = getField<String>(doc, "username") ?: "Anonim"
                        val userProfileImageUrl = getField<String>(doc, "userProfileImageUrl")
                        val isOfficial = getField<Boolean>(doc, "isOfficial") ?: false
                        val badgeType = getField<String>(doc, "userBadgeType") ?: "NONE"
                        val reactionCount = getField<Int>(doc, "reactionCount") ?: 0
                        val viewCount = getField<Long>(doc, "viewCount") ?: 0L
                        val createdAt = getField<Long>(doc, "createdAt") ?: getField<Long>(doc, "createdAtMillis") ?: 0L

                        SharedFeedItem(
                            id = doc.id,
                            type = ContentType.VIDEO,
                            title = title,
                            description = note,
                            mediaUrl = url,
                            ownerId = ownerId,
                            username = username,
                            userProfileImageUrl = userProfileImageUrl,
                            userBadgeType = badgeType,
                            isOfficial = isOfficial,
                            topEmoji = "👏",
                            reactionCount = reactionCount,
                            viewCount = viewCount,
                            createdAtMillis = createdAt
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { emit(emptyList()) }

        val seslerFlow: Flow<List<SharedFeedItem>> = firestore.collection("sesler")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val isPublished = getField<Boolean>(doc, "is_published") ?: true
                        val isFlagged = getField<Boolean>(doc, "is_flagged") ?: false
                        if (!isPublished || isFlagged) return@mapNotNull null

                        val url = getField<String>(doc, "audioUrl") ?: ""
                        val title = getField<String>(doc, "title") ?: "Ses Kaydı"
                        val note = getField<String>(doc, "personalNote") ?: ""
                        val ownerId = getField<String>(doc, "ownerId") ?: ""
                        val username = getField<String>(doc, "username") ?: "Anonim"
                        val userProfileImageUrl = getField<String>(doc, "userProfileImageUrl")
                        val isOfficial = getField<Boolean>(doc, "isOfficial") ?: false
                        val badgeType = getField<String>(doc, "userBadgeType") ?: "NONE"
                        val reactionCount = getField<Int>(doc, "reactionCount") ?: 0
                        val viewCount = getField<Long>(doc, "viewCount") ?: 0L
                        val createdAt = getField<Long>(doc, "createdAt") ?: getField<Long>(doc, "createdAtMillis") ?: 0L

                        SharedFeedItem(
                            id = doc.id,
                            type = ContentType.AUDIO,
                            title = title,
                            description = note,
                            mediaUrl = url,
                            ownerId = ownerId,
                            username = username,
                            userProfileImageUrl = userProfileImageUrl,
                            userBadgeType = badgeType,
                            isOfficial = isOfficial,
                            topEmoji = "🎙️",
                            reactionCount = reactionCount,
                            viewCount = viewCount,
                            createdAtMillis = createdAt
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { emit(emptyList()) }

        return combine(lettersFlow, photosFlow, videosFlow, seslerFlow) { letters, photos, videos, audios ->
            val fullList = mutableListOf<SharedFeedItem>()
            fullList.addAll(letters)
            fullList.addAll(photos)
            fullList.addAll(videos)
            fullList.addAll(audios)
            fullList.sortedByDescending { it.createdAtMillis }
        }
    }
}
