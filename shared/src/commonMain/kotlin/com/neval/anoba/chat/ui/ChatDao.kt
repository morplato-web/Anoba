package com.neval.anoba.chat.ui

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ChatDao {

    // --- GENEL SOHBET ---
    @Query("SELECT * FROM general_messages ORDER BY timestampMillis DESC")
    fun getGeneralMessages(): PagingSource<Int, GeneralChatMessageEntity>

    @Query("SELECT * FROM general_messages WHERE content LIKE '%' || :searchQuery || '%' ORDER BY timestampMillis DESC")
    fun searchGeneralMessages(searchQuery: String): PagingSource<Int, GeneralChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralMessages(messages: List<GeneralChatMessageEntity>)

    @Query("DELETE FROM general_messages WHERE id = :messageId")
    suspend fun deleteGeneralMessageById(messageId: String)

    @Query("DELETE FROM general_messages")
    suspend fun clearGeneralMessages()

    // --- GRUP SOHBETİ ---
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestampMillis DESC")
    fun getGroupMessages(groupId: String): PagingSource<Int, GroupMessageEntity>

    @Query("SELECT MIN(timestampMillis) FROM group_messages WHERE groupId = :groupId")
    suspend fun getOldestGroupMessageTimestamp(groupId: String): Long?

    @Query("SELECT MIN(timestampMillis) FROM private_messages WHERE (senderId = :uid1 AND receiverId = :uid2) OR (senderId = :uid2 AND receiverId = :uid1)")
    suspend fun getOldestPrivateMessageTimestamp(uid1: String, uid2: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessages(messages: List<GroupMessageEntity>)

    @Query("DELETE FROM group_messages WHERE id = :messageId")
    suspend fun deleteGroupMessageById(messageId: String)

    // --- ÖZEL SOHBET ---
    @Query("SELECT * FROM private_messages WHERE (senderId = :myId AND receiverId = :partnerId) OR (senderId = :partnerId AND receiverId = :myId) ORDER BY timestampMillis DESC")
    fun getPrivateMessages(myId: String, partnerId: String): PagingSource<Int, PrivateMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrivateMessages(messages: List<PrivateMessageEntity>)

    @Query("DELETE FROM private_messages WHERE id = :messageId")
    suspend fun deletePrivateMessageById(messageId: String)

    @Transaction
    suspend fun clearAllMessages() {
        clearGeneralMessages()
        clearAllGroupMessages()
        clearAllPrivateMessages()
    }

    @Query("DELETE FROM group_messages")
    suspend fun clearAllGroupMessages()

    @Query("DELETE FROM private_messages")
    suspend fun clearAllPrivateMessages()
}
