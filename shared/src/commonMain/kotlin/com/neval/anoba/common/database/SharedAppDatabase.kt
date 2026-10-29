@file:Suppress("unused", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "NO_ACTUAL_FOR_EXPECT")

package com.neval.anoba.common.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.neval.anoba.chat.data.ChatDao
import com.neval.anoba.chat.data.GeneralChatMessageEntity
import com.neval.anoba.chat.data.GroupMessageEntity
import com.neval.anoba.chat.data.PrivateMessageEntity
import com.neval.anoba.follow.FollowedUserDao
import com.neval.anoba.follow.FollowedUserEntity
import com.neval.anoba.letter.SharedLetterDao
import com.neval.anoba.letter.SharedLetterModel
import com.neval.anoba.market.SharedMarketDao
import com.neval.anoba.market.SharedMarketItemModel

@Database(
    entities = [
        GeneralChatMessageEntity::class,
        GroupMessageEntity::class,
        PrivateMessageEntity::class,
        PinnedItemEntity::class,
        FollowedUserEntity::class,
        SharedMarketItemModel::class,
        SharedLetterModel::class,
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
    abstract fun marketDao(): SharedMarketDao
    abstract fun letterDao(): SharedLetterDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SharedAppDatabaseConstructor : RoomDatabaseConstructor<SharedAppDatabase> {
    override fun initialize(): SharedAppDatabase
}
