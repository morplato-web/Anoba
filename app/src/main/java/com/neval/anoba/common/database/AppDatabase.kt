package com.neval.anoba.common.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.neval.anoba.chat.data.ChatDao
import com.neval.anoba.chat.data.GeneralChatMessageEntity
import com.neval.anoba.chat.data.GroupMessageEntity
import com.neval.anoba.chat.data.PrivateMessageEntity
import com.neval.anoba.follow.FollowedUserDao
import com.neval.anoba.follow.FollowedUserEntity
import com.neval.anoba.letter.LetterDao
import com.neval.anoba.letter.LetterModel
import com.neval.anoba.market.MarketDao
import com.neval.anoba.market.MarketItemModel
import com.neval.anoba.photo.PhotoDao
import com.neval.anoba.photo.PhotoModel
import com.neval.anoba.ses.SesDao
import com.neval.anoba.ses.SesModel
import com.neval.anoba.video.VideoDao
import com.neval.anoba.video.VideoModel

@Database(
    entities = [
        SesModel::class,
        PhotoModel::class,
        VideoModel::class,
        LetterModel::class,
        GeneralChatMessageEntity::class,
        GroupMessageEntity::class,
        PrivateMessageEntity::class,
        PinnedItemEntity::class,
        FollowedUserEntity::class,
        MarketItemModel::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sesDao(): SesDao
    abstract fun photoDao(): PhotoDao
    abstract fun videoDao(): VideoDao
    abstract fun letterDao(): LetterDao
    abstract fun chatDao(): ChatDao
    abstract fun pinnedItemDao(): PinnedItemDao
    abstract fun followedUserDao(): FollowedUserDao
    abstract fun marketDao(): MarketDao
}
