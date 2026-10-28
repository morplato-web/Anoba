package com.neval.anoba.mycontent

enum class SharedMyContentType {
    LETTER, PHOTO, VIDEO, AUDIO, MARKET
}

sealed class SharedMyContentItem {
    abstract val id: String
    abstract val type: SharedMyContentType
    abstract val isPublished: Boolean
    abstract val isFlagged: Boolean
    abstract val isGuideContent: Boolean
    abstract val viewCount: Long
    abstract val reactionsCount: Long
    abstract val commentsCount: Long
    abstract val personalNote: String
    abstract val thumbnailUrl: String?

    data class Letter(
        override val id: String,
        override val isPublished: Boolean,
        override val isFlagged: Boolean = false,
        override val isGuideContent: Boolean = false,
        override val viewCount: Long = 0,
        override val reactionsCount: Long = 0,
        override val commentsCount: Long = 0,
        override val personalNote: String = "",
        val content: String = "",
        val audioUrl: String? = null,
        val audioDuration: Long? = null,
        val privacy: String? = null,
        val recipientUsername: String? = null,
        val isTimeCapsule: Boolean = false
    ) : SharedMyContentItem() {
        override val type: SharedMyContentType get() = SharedMyContentType.LETTER
        override val thumbnailUrl: String? get() = null
    }

    data class Photo(
        override val id: String,
        override val isPublished: Boolean,
        override val isFlagged: Boolean = false,
        override val isGuideContent: Boolean = false,
        override val viewCount: Long = 0,
        override val reactionsCount: Long = 0,
        override val commentsCount: Long = 0,
        override val personalNote: String = "",
        val photoUrl: String = ""
    ) : SharedMyContentItem() {
        override val type: SharedMyContentType get() = SharedMyContentType.PHOTO
        override val thumbnailUrl: String get() = photoUrl
    }

    data class Video(
        override val id: String,
        override val isPublished: Boolean,
        override val isFlagged: Boolean = false,
        override val isGuideContent: Boolean = false,
        override val viewCount: Long = 0,
        override val reactionsCount: Long = 0,
        override val commentsCount: Long = 0,
        override val personalNote: String = "",
        val videoUrl: String = "",
        override val thumbnailUrl: String? = null
    ) : SharedMyContentItem() {
        override val type: SharedMyContentType get() = SharedMyContentType.VIDEO
    }

    data class Audio(
        override val id: String,
        override val isPublished: Boolean,
        override val isFlagged: Boolean = false,
        override val isGuideContent: Boolean = false,
        override val viewCount: Long = 0,
        override val reactionsCount: Long = 0,
        override val commentsCount: Long = 0,
        override val personalNote: String = "",
        val audioUrl: String = "",
        val durationMillis: Long = 0L
    ) : SharedMyContentItem() {
        override val type: SharedMyContentType get() = SharedMyContentType.AUDIO
        override val thumbnailUrl: String? get() = null
    }

    data class Market(
        override val id: String,
        override val isPublished: Boolean,
        override val isFlagged: Boolean = false,
        override val isGuideContent: Boolean = false,
        override val viewCount: Long = 0,
        override val reactionsCount: Long = 0,
        override val commentsCount: Long = 0,
        override val personalNote: String = "",
        val title: String = "",
        val photoUrl: String = "",
        val price: String = ""
    ) : SharedMyContentItem() {
        override val type: SharedMyContentType get() = SharedMyContentType.MARKET
        override val thumbnailUrl: String get() = photoUrl
    }
}
