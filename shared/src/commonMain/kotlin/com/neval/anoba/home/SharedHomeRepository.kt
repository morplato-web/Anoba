package com.neval.anoba.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SharedHomeRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val projectId = "anoba-cd9de"
    private val baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    fun getCombinedHomeFeed(): Flow<List<SharedFeedItem>> = flow {
        val letters = fetchCollection("letters", ContentType.LETTER)
        val photos = fetchCollection("photos", ContentType.PHOTO)
        val videos = fetchCollection("videos", ContentType.VIDEO)
        val audios = fetchCollection("sesler", ContentType.AUDIO)

        val fullList = mutableListOf<SharedFeedItem>()
        fullList.addAll(letters)
        fullList.addAll(photos)
        fullList.addAll(videos)
        fullList.addAll(audios)

        emit(fullList.sortedByDescending { it.createdAtMillis })
    }.flowOn(Dispatchers.IO)

    private fun fetchCollection(collectionName: String, contentType: ContentType): List<SharedFeedItem> {
        val url = "$baseUrl/$collectionName?pageSize=30"
        val rawJson = httpGet(url)
        if (rawJson.isBlank()) return emptyList()

        return try {
            val root = json.parseToJsonElement(rawJson).jsonObject
            val documents = root["documents"]?.jsonArray ?: return emptyList()

            documents.mapNotNull { docElement ->
                try {
                    val docObj = docElement.jsonObject
                    val namePath = docObj["name"]?.jsonPrimitive?.content ?: ""
                    val docId = namePath.substringAfterLast('/')

                    val fields = docObj["fields"]?.jsonObject ?: return@mapNotNull null

                    val isPublished = parseBoolean(fields, "is_published", default = true)
                    val isFlagged = parseBoolean(fields, "is_flagged", default = false)
                    if (!isPublished || isFlagged) return@mapNotNull null

                    val title = parseString(fields, "title")
                    val text = parseString(fields, "text").ifBlank { parseString(fields, "personalNote") }
                    val finalTitle = title.ifBlank { if (contentType == ContentType.LETTER) text.take(30) else defaultTitle(contentType) }

                    val photoUrl = parseString(fields, "photoUrl").ifBlank { parseString(fields, "url") }
                    val videoUrl = parseString(fields, "videoUrl")
                    val audioUrl = parseString(fields, "audioUrl")
                    val mediaUrl = photoUrl.ifBlank { videoUrl }.ifBlank { audioUrl }.takeIf { it.isNotBlank() }

                    val ownerId = parseString(fields, "ownerId")
                    val username = parseString(fields, "username").ifBlank { "Anonim" }
                    val userProfileImageUrl = parseString(fields, "userProfileImageUrl").takeIf { it.isNotBlank() }
                    val isOfficial = parseBoolean(fields, "isOfficial", default = false)
                    val badgeType = parseString(fields, "userBadgeType").ifBlank { "NONE" }
                    val reactionCount = parseLong(fields, "reactionCount").toInt()
                    val viewCount = parseLong(fields, "viewCount")
                    val createdAt = parseLong(fields, "createdAt").let { if (it == 0L) parseLong(fields, "createdAtMillis") else it }

                    SharedFeedItem(
                        id = docId,
                        type = contentType,
                        title = finalTitle,
                        description = text,
                        mediaUrl = mediaUrl,
                        ownerId = ownerId,
                        username = username,
                        userProfileImageUrl = userProfileImageUrl,
                        userBadgeType = badgeType,
                        isOfficial = isOfficial,
                        topEmoji = defaultEmoji(contentType),
                        reactionCount = reactionCount,
                        viewCount = viewCount,
                        createdAtMillis = createdAt
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseString(fields: kotlinx.serialization.json.JsonObject, fieldName: String): String {
        return fields[fieldName]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
    }

    private fun parseBoolean(fields: kotlinx.serialization.json.JsonObject, fieldName: String, default: Boolean): Boolean {
        return fields[fieldName]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: default
    }

    private fun parseLong(fields: kotlinx.serialization.json.JsonObject, fieldName: String): Long {
        val obj = fields[fieldName]?.jsonObject ?: return 0L
        return obj["integerValue"]?.jsonPrimitive?.content?.toLongOrNull()
            ?: obj["doubleValue"]?.jsonPrimitive?.content?.toDoubleOrNull()?.toLong()
            ?: 0L
    }

    private fun defaultTitle(type: ContentType): String = when (type) {
        ContentType.PHOTO -> "Fotoğraf"
        ContentType.VIDEO -> "Video"
        ContentType.AUDIO -> "Ses Kaydı"
        ContentType.LETTER -> "Mektup"
        ContentType.MARKET -> "İlan"
    }

    private fun defaultEmoji(type: ContentType): String = when (type) {
        ContentType.PHOTO -> "🔥"
        ContentType.VIDEO -> "👏"
        ContentType.AUDIO -> "🎙️"
        ContentType.LETTER -> "❤️"
        ContentType.MARKET -> "🛍️"
    }
}
