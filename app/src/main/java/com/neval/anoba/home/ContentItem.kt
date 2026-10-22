package com.neval.anoba.home

import com.neval.anoba.letter.LetterModel
import com.neval.anoba.market.MarketItemModel
import com.neval.anoba.photo.PhotoModel
import com.neval.anoba.ses.SesModel
import com.neval.anoba.video.VideoModel
import java.util.Date

sealed class ContentItem {
    abstract val id: String
    abstract val contentType: String
    abstract val createdAt: Date?
    abstract val publishTimestamp: Date?
    abstract val isPublished: Boolean
    abstract val isOfficial: Boolean
    abstract val viewCount: Long

    val sortDate: Date get() = publishTimestamp ?: createdAt ?: Date()

    data class PhotoContent(val photo: PhotoModel) : ContentItem() {
        override val id: String get() = photo.id
        override val contentType: String get() = "photo"
        override val createdAt: Date? get() = photo.createdAt
        override val publishTimestamp: Date? get() = photo.publishTimestamp
        override val isPublished: Boolean get() = photo.isPublished
        override val isOfficial: Boolean get() = photo.isOfficial
        override val viewCount: Long get() = photo.viewCount
    }

    data class LetterContent(val letter: LetterModel) : ContentItem() {
        override val id: String get() = letter.id
        override val contentType: String get() = "letter"
        override val createdAt: Date? get() = letter.createdAt
        override val publishTimestamp: Date? get() = letter.publishTimestamp
        override val isPublished: Boolean get() = letter.isPublished
        override val isOfficial: Boolean get() = letter.isOfficial
        override val viewCount: Long get() = letter.viewCount
    }

    data class AudioContent(val audio: SesModel) : ContentItem() {
        override val id: String get() = audio.id
        override val contentType: String get() = "audio"
        override val createdAt: Date? get() = audio.createdAt
        override val publishTimestamp: Date? get() = audio.publishTimestamp
        override val isPublished: Boolean get() = audio.isPublished
        override val isOfficial: Boolean get() = audio.isOfficial
        override val viewCount: Long get() = audio.viewCount
    }

    data class VideoContent(val video: VideoModel) : ContentItem() {
        override val id: String get() = video.id
        override val contentType: String get() = "video"
        override val createdAt: Date? get() = video.createdAt
        override val publishTimestamp: Date? get() = video.publishTimestamp
        override val isPublished: Boolean get() = video.isPublished
        override val isOfficial: Boolean get() = video.isOfficial
        override val viewCount: Long get() = video.viewCount
    }

    data class MarketContent(val marketItem: MarketItemModel) : ContentItem() {
        override val id: String get() = marketItem.id
        override val contentType: String get() = "market"
        override val createdAt: Date? get() = marketItem.createdAt
        override val publishTimestamp: Date? get() = marketItem.createdAt
        override val isPublished: Boolean get() = !marketItem.isSold
        override val isOfficial: Boolean get() = marketItem.isOfficial
        override val viewCount: Long get() = 0
    }
}