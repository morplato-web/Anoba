package com.neval.anoba.chat.group

data class SharedChatGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val isPrivate: Boolean = false,
    val members: List<String> = emptyList(),
    val imageUrl: String? = null,
    val typingUsers: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val createdAtMillis: Long = 0L,
) {
    val memberCount: Int
        get() = members.size
}

typealias SharedGroupItem = SharedChatGroup
