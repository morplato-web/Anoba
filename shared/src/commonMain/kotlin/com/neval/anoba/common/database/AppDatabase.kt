package com.neval.anoba.common.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.neval.anoba.chat.ui.ChatDao
import com.neval.anoba.chat.ui.GeneralChatMessageEntity
import com.neval.anoba.chat.ui.GroupMessageEntity
import com.neval.anoba.chat.ui.PrivateMessageEntity
import com.neval.anoba.follow.FollowedUserDao
import com.neval.anoba.follow.FollowedUserEntity
import com.neval.anoba.letter.LetterDao
import com.neval.anoba.letter.LetterModel
import com.neval.anoba.market.MarketDao
import com.neval.anoba.market.MarketItemModel

@Database(
    entities = [
        GeneralChatMessageEntity::class,
        GroupMessageEntity::class,
        PrivateMessageEntity::class,
        PinnedItemEntity::class,
        FollowedUserEntity::class,
        MarketItemModel::class,
        LetterModel::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(SharedConverters::class)
@ConstructedBy(SharedAppDatabaseConstructor::class)
abstract class SharedAppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun pinnedItemDao(): PinnedItemDao
    abstract fun followedUserDao(): FollowedUserDao
    abstract fun marketDao(): MarketDao
    abstract fun letterDao(): LetterDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SharedAppDatabaseConstructor : RoomDatabaseConstructor<SharedAppDatabase> {
    override fun initialize(): SharedAppDatabase
}
