package com.neval.anoba.chat.group

/**
 * Anoba Grup Sohbeti Rol Hiyerarşisi
 */
enum class GroupChatUserRole {
    ADMIN,     // Grup sahibi, tam yetkili.
    MODERATOR, // Yardımcı yönetici, mesaj silebilir.
    MEMBER,    // Normal kayıtlı katılımcı.
    GUEST      // Kayıtsız veya izleyici statüsü.
}
