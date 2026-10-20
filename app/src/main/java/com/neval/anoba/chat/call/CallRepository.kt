package com.neval.anoba.chat.call

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CallRepository(
    private val firestore: FirebaseFirestore
) {
    private val tag = "CallRepository"

    suspend fun createCall(call: CallModel) {
        try {
            firestore.collection("calls").document(call.id).set(call).await()
        } catch (e: Exception) {
            Log.e(tag, "Error creating call", e)
            throw e
        }
    }

    suspend fun updateCallStatus(docId: String, status: CallStatus) {
        try {
            firestore.collection("calls").document(docId).update("status", status.name).await()
        } catch (e: Exception) {
            Log.e(tag, "Error updating call status", e)
        }
    }

    fun listenToCall(docId: String): Flow<CallModel?> = callbackFlow {
        val subscription = firestore.collection("calls").document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val call = snapshot?.toObject(CallModel::class.java)
                trySend(call)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun findActiveRingingCall(channelName: String, receiverId: String): String? {
        return try {
            val snapshot = firestore.collection("calls")
                .whereEqualTo("channelName", channelName)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("status", CallStatus.RINGING.name)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e(tag, "Error finding active call", e)
            null
        }
    }

    suspend fun getPartnerInfo(partnerId: String): Pair<String?, String?> {
        return try {
            val doc = firestore.collection("users").document(partnerId).get().await()
            val name = doc.getString("username")?.ifBlank {
                doc.getString("displayName")
            } ?: doc.getString("displayName")

            // 🚀 DÜZELTME: Anoba veri modelindeki tüm varyasyonlar kontrol ediliyor
            val photo = doc.getString("profileImageUrl")
                ?: doc.getString("photoUrl")
                ?: doc.getString("photoURL")

            Pair(name, photo)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching partner info", e)
            Pair(null, null)
        }
    }

    suspend fun endAnyActiveCall(channelName: String) {
        try {
            val snapshot = firestore.collection("calls")
                .whereEqualTo("channelName", channelName)
                .whereIn("status", listOf(CallStatus.RINGING.name, CallStatus.ACCEPTED.name))
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "status", CallStatus.ENDED.name)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error ending active calls", e)
        }
    }
}