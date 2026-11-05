package com.neval.anoba.community.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.community.model.ChannelModel
import com.neval.anoba.community.model.ChannelType
import com.neval.anoba.community.model.ServerModel
import com.neval.anoba.community.model.VoiceMemberModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommunityRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Firestore bağımsız izole koleksiyonları
    private val serversRef = firestore.collection("community_servers")

    fun getServersStream(): Flow<List<ServerModel>> = callbackFlow {
        val listener = serversRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val servers = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ServerModel::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(servers)
        }
        awaitClose { listener.remove() }
    }

    fun getChannelsStream(serverId: String): Flow<List<ChannelModel>> = callbackFlow {
        if (serverId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = serversRef.document(serverId).collection("channels")
            .orderBy("position")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val channels = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChannelModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(channels)
            }
        awaitClose { listener.remove() }
    }

    fun getVoiceMembersStream(serverId: String, channelId: String): Flow<List<VoiceMemberModel>> = callbackFlow {
        if (serverId.isBlank() || channelId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = serversRef.document(serverId)
            .collection("channels").document(channelId)
            .collection("voice_members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VoiceMemberModel::class.java)
                } ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    suspend fun joinVoiceChannel(serverId: String, channelId: String, member: VoiceMemberModel) {
        if (serverId.isBlank() || channelId.isBlank() || member.userId.isBlank()) return
        serversRef.document(serverId)
            .collection("channels").document(channelId)
            .collection("voice_members").document(member.userId)
            .set(member)
            .await()
    }

    suspend fun leaveVoiceChannel(serverId: String, channelId: String, userId: String) {
        if (serverId.isBlank() || channelId.isBlank() || userId.isBlank()) return
        serversRef.document(serverId)
            .collection("channels").document(channelId)
            .collection("voice_members").document(userId)
            .delete()
            .await()
    }

    suspend fun updateVoiceState(
        serverId: String,
        channelId: String,
        userId: String,
        isMuted: Boolean,
        isDeafened: Boolean,
        isSpeaking: Boolean
    ) {
        if (serverId.isBlank() || channelId.isBlank() || userId.isBlank()) return
        val updates = mapOf(
            "isMuted" to isMuted,
            "isDeafened" to isDeafened,
            "isSpeaking" to isSpeaking
        )
        serversRef.document(serverId)
            .collection("channels").document(channelId)
            .collection("voice_members").document(userId)
            .update(updates)
            .await()
    }

    suspend fun createDefaultSampleServersIfEmpty() {
        val snapshot = serversRef.get().await()
        if (!snapshot.isEmpty) return

        // Örnek Sunucu 1: Anoba Gaming 🎮
        val server1 = ServerModel(
            name = "Anoba Gaming 🎮",
            description = "PUBG, Valorant ve CS topluluğu",
            iconUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc",
            memberCount = 128
        )
        val s1Doc = serversRef.document()
        val s1Id = s1Doc.id
        s1Doc.set(server1.copy(id = s1Id)).await()

        s1Doc.collection("channels").add(ChannelModel(serverId = s1Id, name = "genel-sohbet", type = ChannelType.TEXT, position = 0)).await()
        s1Doc.collection("channels").add(ChannelModel(serverId = s1Id, name = "oyun-duyuru", type = ChannelType.TEXT, position = 1)).await()
        s1Doc.collection("channels").add(ChannelModel(serverId = s1Id, name = "Bilek Kadro", type = ChannelType.VOICE, position = 2)).await()
        s1Doc.collection("channels").add(ChannelModel(serverId = s1Id, name = "PUBG Squad", type = ChannelType.VOICE, position = 3)).await()
        s1Doc.collection("channels").add(ChannelModel(serverId = s1Id, name = "Kurucu Odası", type = ChannelType.VOICE, position = 4)).await()

        // Örnek Sunucu 2: Serbest Alan ☕
        val server2 = ServerModel(
            name = "Serbest Alan ☕",
            description = "Sohbet, Müzik ve Muhabbet",
            iconUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4",
            memberCount = 64
        )
        val s2Doc = serversRef.document()
        val s2Id = s2Doc.id
        s2Doc.set(server2.copy(id = s2Id)).await()

        s2Doc.collection("channels").add(ChannelModel(serverId = s2Id, name = "muhabbet", type = ChannelType.TEXT, position = 0)).await()
        s2Doc.collection("channels").add(ChannelModel(serverId = s2Id, name = "Müzik & Ses", type = ChannelType.VOICE, position = 1)).await()
    }
}
