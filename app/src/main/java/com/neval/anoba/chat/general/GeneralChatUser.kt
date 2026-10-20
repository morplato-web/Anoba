package com.neval.anoba.chat.general

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.neval.anoba.chat.group.GroupChatUserRole
import java.util.Date

@Keep
@IgnoreExtraProperties
data class GeneralChatUser(
    @DocumentId
    var id: String = "",

    @get:PropertyName("displayName")
    @set:PropertyName("displayName")
    var displayName: String? = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String? = null,

    @get:PropertyName("photoUrl")
    @set:PropertyName("photoUrl")
    var photoUrl: String? = null,

    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: String? = GroupChatUserRole.MEMBER.name,

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String? = null,

    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String? = null,

    @get:PropertyName("username")
    @set:PropertyName("username")
    var username: String? = null,

    @get:PropertyName("bio")
    @set:PropertyName("bio")
    var bio: String? = null,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Date? = null,

    @get:PropertyName("userNumber")
    @set:PropertyName("userNumber")
    var userNumber: String? = null,

    @get:PropertyName("badgeType")
    @set:PropertyName("badgeType")
    var badgeType: String? = "NONE",

    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,
) {
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