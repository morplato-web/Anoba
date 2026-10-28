package com.neval.anoba.chat.call

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.util.Date

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

// Android -> KMP Dönüştürücü
@Suppress("unused")
fun CallModel.toSharedModel(): SharedCallModel {
    return SharedCallModel(
        id = this.id,
        callerId = this.callerId,
        receiverId = this.receiverId,
        channelName = this.channelName,
        isVideoCall = this.isVideoCall,
        status = this.status,
        timestampMillis = this.timestamp?.toDate()?.time ?: 0L
    )
}

// KMP -> Android Geri Dönüştürücü (Durum güncellemeleri için)
@Suppress("unused")
fun SharedCallModel.toAndroidModel(): CallModel {
    return CallModel(
        id = this.id,
        callerId = this.callerId,
        receiverId = this.receiverId,
        channelName = this.channelName,
        isVideoCall = this.isVideoCall,
        status = this.status,
        timestamp = if (this.timestampMillis > 0L) Timestamp(Date(this.timestampMillis)) else null
    )
}
