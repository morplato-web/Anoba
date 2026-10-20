package com.neval.anoba.chat.call

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import androidx.annotation.Keep

@Keep
data class CallModel(
    var id: String = "",
    var callerId: String = "",
    var receiverId: String = "",
    var channelName: String = "",
    @get:PropertyName("isVideoCall") @set:PropertyName("isVideoCall") var isVideoCall: Boolean = true,
    var status: String = CallStatus.RINGING.name,
    var timestamp: Timestamp? = null
)

enum class CallStatus {
    RINGING,
    ACCEPTED,
    REJECTED,
    ENDED
}
