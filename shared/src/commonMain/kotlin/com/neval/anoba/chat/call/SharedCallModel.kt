package com.neval.anoba.chat.call

enum class SharedCallStatus {
    RINGING,
    ACCEPTED,
    REJECTED,
    ENDED;

    companion object {
        fun fromString(status: String?): SharedCallStatus {
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: RINGING
        }
    }
}

data class SharedCallModel(
    val id: String = "",
    val callerId: String = "",
    val receiverId: String = "",
    val channelName: String = "",
    val isVideoCall: Boolean = true,
    val status: String = SharedCallStatus.RINGING.name,
    val timestampMillis: Long = 0L
) {
    val safeStatus: SharedCallStatus
        get() = SharedCallStatus.fromString(status)

    @Suppress("unused")
    val isRinging: Boolean
        get() = safeStatus == SharedCallStatus.RINGING

    @Suppress("unused")
    val isOngoing: Boolean
        get() = safeStatus == SharedCallStatus.ACCEPTED

    @Suppress("unused")
    val isFinished: Boolean
        get() = safeStatus == SharedCallStatus.REJECTED || safeStatus == SharedCallStatus.ENDED
}
