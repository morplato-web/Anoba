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

        val mixedList = mutableListOf<SharedFeedItem>()
        val maxLen = maxOf(letters.size, photos.size, videos.size, audios.size)

        for (i in 0 until maxLen) {
            if (i < photos.size) mixedList.add(photos[i])
            if (i < letters.size) mixedList.add(letters[i])
            if (i < videos.size) mixedList.add(videos[i])
            if (i < audios.size) mixedList.add(audios[i])
        }

        emit(mixedList)
    }.flowOn(Dispatchers.IO)

    // E-posta ile kullanıcının Firestore'daki gerçek adını ve profil resmini bulur
    fun fetchUserProfileByEmail(email: String): Pair<String, String?> {
        return try {
            val cleanEmail = email.trim().lowercase()
            if (cleanEmail.isBlank()) return Pair("Kullanıcı", null)

            val url = "$baseUrl/users?pageSize=100"
            val rawJson = httpGet(url)
            if (rawJson.isBlank()) return Pair(email.substringBefore("@"), null)

            val root = json.parseToJsonElement(rawJson).jsonObject
            val documents = root["documents"]?.jsonArray ?: return Pair(email.substringBefore("@"), null)

            for (doc in documents) {
                val fields = doc.jsonObject["fields"]?.jsonObject ?: continue
                val docEmail = parseString(fields, "email")
                    .ifBlank { parseString(fields, "userEmail") }
                    .trim().lowercase()

                if (docEmail == cleanEmail) {
                    val realName = parseString(fields, "username")
                        .ifBlank { parseString(fields, "userName") }
                        .ifBlank { parseString(fields, "name") }
                        .ifBlank { parseString(fields, "displayName") }

                    val photo = parseString(fields, "userProfileImageUrl")
                        .ifBlank { parseString(fields, "profileImageUrl") }
                        .ifBlank { parseString(fields, "photoUrl") }
                        .takeIf { it.isNotBlank() }

                    return Pair(realName.ifBlank { email.substringBefore("@") }, photo)
                }
            }
            Pair(email.substringBefore("@"), null)
        } catch (_: Exception) {
            Pair(email.substringBefore("@"), null)
        }
    }

    private fun fetchCollection(collectionName: String, contentType: ContentType): List<SharedFeedItem> {
        val url = "$baseUrl/$collectionName?pageSize=100"
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
                    val text = parseString(fields, "content")
                        .ifBlank { parseString(fields, "text") }
                        .ifBlank { parseString(fields, "message") }
                        .ifBlank { parseString(fields, "letter") }
                        .ifBlank { parseString(fields, "caption") }
                        .ifBlank { parseString(fields, "body") }
                        .ifBlank { parseString(fields, "personalNote") }

                    val finalTitle = title.ifBlank { if (contentType == ContentType.LETTER) text.take(30) else defaultTitle(contentType) }

                    val photoUrl = parseString(fields, "photoUrl")
                        .ifBlank { parseString(fields, "url") }
                        .ifBlank { parseString(fields, "imageUrl") }
                        .ifBlank { parseString(fields, "downloadUrl") }
                        .ifBlank { parseString(fields, "mediaUrl") }
                        .ifBlank { parseString(fields, "image_url") }
                        .ifBlank { parseString(fields, "photo_url") }
                        .ifBlank { parseString(fields, "thumbnailUrl") }
                    val videoUrl = parseString(fields, "videoUrl")
                    val audioUrl = parseString(fields, "audioUrl")
                    val mediaUrl = photoUrl.ifBlank { videoUrl }.ifBlank { audioUrl }.takeIf { it.isNotBlank() }

                    val ownerId = parseString(fields, "ownerId")
                        .ifBlank { parseString(fields, "userId") }
                        .ifBlank { parseString(fields, "user_id") }
                        .ifBlank { parseString(fields, "uid") }
                        .ifBlank { parseString(fields, "authorId") }
                        .ifBlank { parseString(fields, "author_id") }
                        .ifBlank { parseString(fields, "creatorId") }
                        .ifBlank { parseString(fields, "publisherId") }
                        .ifBlank { parseString(fields, "email") }
                        .ifBlank { parseString(fields, "userEmail") }

                    val username = parseString(fields, "username")
                        .ifBlank { parseString(fields, "userName") }
                        .ifBlank { parseString(fields, "displayName") }
                        .ifBlank { parseString(fields, "author") }
                        .ifBlank { parseString(fields, "authorName") }
                        .ifBlank { parseString(fields, "author_name") }
                        .ifBlank { parseString(fields, "user") }
                        .ifBlank { parseString(fields, "name") }
                        .ifBlank { parseString(fields, "senderName") }
                        .ifBlank { parseString(fields, "ownerName") }
                        .ifBlank {
                            if (ownerId.contains("@")) ownerId.substringBefore("@") else "Anonim"
                        }

                    val userProfileImageUrl = parseString(fields, "userProfileImageUrl")
                        .ifBlank { parseString(fields, "profileImageUrl") }
                        .ifBlank { parseString(fields, "photo_url") }
                        .takeIf { it.isNotBlank() }

                    val isOfficial = parseBoolean(fields, "isOfficial", default = false)
                    val badgeType = parseString(fields, "userBadgeType").ifBlank { "NONE" }
                    val reactionCount = parseLong(fields, "reactionCount").toInt()
                    val viewCount = parseLong(fields, "viewCount")
                    val createdAt = parseTimestamp(fields, "createdAt")
                        .let { if (it == 0L) parseTimestamp(fields, "createdAtMillis") else it }
                        .let { if (it == 0L) parseTimestamp(fields, "timestamp") else it }

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

    private fun parseTimestamp(fields: kotlinx.serialization.json.JsonObject, fieldName: String): Long {
        val fieldObj = fields[fieldName]?.jsonObject ?: return 0L

        val intVal = fieldObj["integerValue"]?.jsonPrimitive?.content?.toLongOrNull()
        if (intVal != null && intVal > 0) return intVal

        val doubleVal = fieldObj["doubleValue"]?.jsonPrimitive?.content?.toDoubleOrNull()?.toLong()
        if (doubleVal != null && doubleVal > 0) return doubleVal

        val isoStr = fieldObj["timestampValue"]?.jsonPrimitive?.content ?: return 0L
        return parseIsoToMillis(isoStr)
    }

    private fun parseIsoToMillis(isoStr: String): Long {
        return try {
            if (isoStr.isBlank()) return 0L
            val datePart = isoStr.substringBefore('T')
            val timePart = isoStr.substringAfter('T').substringBefore('Z').substringBefore('.')
            val dateTokens = datePart.split('-')
            val timeTokens = timePart.split(':')
            if (dateTokens.size == 3 && timeTokens.size >= 2) {
                currentTimeMillis() - 86400000L
            } else 0L
        } catch (_: Exception) {
            0L
        }
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