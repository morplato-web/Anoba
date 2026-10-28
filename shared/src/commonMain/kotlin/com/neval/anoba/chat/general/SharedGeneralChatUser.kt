package com.neval.anoba.chat.general

data class SharedGeneralChatUser(
    val id: String = "",
    val displayName: String? = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val role: String? = "MEMBER",
    val uid: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val createdAtMillis: Long? = null,
    val userNumber: String? = null,
    val badgeType: String? = "NONE",
    val isOfficial: Boolean = false,
) {
    @Suppress("unused")
    val safeDisplayName: String
        get() {
            val raw = displayName?.ifBlank { username ?: "Bilinmiyor" } ?: username ?: "Bilinmiyor"
            val isOfficialAccount = (badgeType == "OFFICIAL") || isOfficial || (email == "morplato@gmail.com")
            return if (isOfficialAccount && !raw.contains("anoba", ignoreCase = true)) {
                "Anoba"
            } else {
                raw
            }
        }
}
