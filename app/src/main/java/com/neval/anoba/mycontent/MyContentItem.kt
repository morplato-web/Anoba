package com.neval.anoba.mycontent

import com.neval.anoba.letter.LetterModel
import com.neval.anoba.market.MarketItemModel
import com.neval.anoba.photo.PhotoModel
import com.neval.anoba.ses.SesModel
import com.neval.anoba.video.VideoModel
import java.util.Date

enum class MyContentType {
    LETTER, PHOTO, VIDEO, AUDIO, MARKET
}

sealed class MyContentItem {
    abstract val id: String
    abstract val type: MyContentType
    abstract val isPublished: Boolean
    abstract val isFlagged: Boolean
    abstract val isGuideContent: Boolean
    abstract val viewCount: Long
    abstract val reactionsCount: Long
    abstract val commentsCount: Long
    abstract val createdAt: Date?
    abstract val publishTimestamp: Date?
    abstract val personalNote: String
    abstract val thumbnailUrl: String?

    // SIRALAMA İÇİN TEK GÜVENİLİR KAYNAK
    val sortDate: Date get() = (if (isPublished) publishTimestamp else createdAt) ?: createdAt ?: Date(0)

    data class Letter(val letter: LetterModel) : MyContentItem() {
        override val id: String get() = letter.id
        override val type: MyContentType get() = MyContentType.LETTER
        override val isPublished: Boolean get() = letter.isPublished
        override val isFlagged: Boolean get() = letter.isFlagged
        override val isGuideContent: Boolean get() = letter.isGuideContent
        override val viewCount: Long get() = letter.viewCount
        override val reactionsCount: Long get() = letter.reactions.values.sum()
        override val commentsCount: Long get() = letter.commentsCount
        override val createdAt: Date? get() = letter.createdAt
        override val publishTimestamp: Date? get() = letter.publishTimestamp
        override val personalNote: String get() = letter.personalNote
        override val thumbnailUrl: String? get() = null
    }

    data class Photo(val photo: PhotoModel) : MyContentItem() {
        override val id: String get() = photo.id
        override val type: MyContentType get() = MyContentType.PHOTO
        override val isPublished: Boolean get() = photo.isPublished
        override val isFlagged: Boolean get() = photo.isFlagged
        override val isGuideContent: Boolean get() = photo.isGuideContent
        override val viewCount: Long get() = photo.viewCount
        override val reactionsCount: Long get() = photo.reactions.values.sum()
        override val commentsCount: Long get() = photo.commentsCount
        override val createdAt: Date? get() = photo.createdAt
        override val publishTimestamp: Date? get() = photo.publishTimestamp
        override val personalNote: String get() = photo.personalNote
        override val thumbnailUrl: String get() = photo.photoUrl
    }

    data class Video(val video: VideoModel) : MyContentItem() {
        override val id: String get() = video.id
        override val type: MyContentType get() = MyContentType.VIDEO
        override val isPublished: Boolean get() = video.isPublished
        override val isFlagged: Boolean get() = video.isFlagged
        override val isGuideContent: Boolean get() = video.isGuideContent
        override val viewCount: Long get() = video.viewCount
        override val reactionsCount: Long get() = video.reactions.values.sum()
        override val commentsCount: Long get() = video.commentsCount
        override val createdAt: Date? get() = video.createdAt
        override val publishTimestamp: Date? get() = video.publishTimestamp
        override val personalNote: String get() = video.personalNote
        override val thumbnailUrl: String? get() = video.thumbnailUrl
    }

    data class Audio(val audio: SesModel) : MyContentItem() {
        override val id: String get() = audio.id
        override val type: MyContentType get() = MyContentType.AUDIO
        override val isPublished: Boolean get() = audio.isPublished
        override val isFlagged: Boolean get() = audio.isFlagged
        override val isGuideContent: Boolean get() = audio.isGuideContent
        override val viewCount: Long get() = audio.viewCount
        override val reactionsCount: Long get() = audio.reactions.values.sum()
        override val commentsCount: Long get() = audio.commentsCount
        override val createdAt: Date? get() = audio.createdAt
        override val publishTimestamp: Date? get() = audio.publishTimestamp
        override val personalNote: String get() = audio.personalNote
        override val thumbnailUrl: String? get() = null
    }

    data class Market(val marketItem: MarketItemModel) : MyContentItem() {
        override val id: String get() = marketItem.id
        override val type: MyContentType get() = MyContentType.MARKET
        override val isPublished: Boolean get() = !marketItem.isSold
        override val isFlagged: Boolean get() = false
        override val isGuideContent: Boolean get() = false
        override val viewCount: Long get() = 0
        override val reactionsCount: Long get() = 0
        override val commentsCount: Long get() = 0
        override val createdAt: Date? get() = marketItem.createdAt
        override val publishTimestamp: Date? get() = marketItem.createdAt
        override val personalNote: String get() = marketItem.title
        override val thumbnailUrl: String get() = marketItem.photoUrl
    }
}
