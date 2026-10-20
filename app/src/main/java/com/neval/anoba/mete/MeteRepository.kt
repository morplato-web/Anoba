package com.neval.anoba.mete

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class MeteRepository(private val firestore: FirebaseFirestore) {

    // Sol panel kelimeleri için akış
    fun getWordsFromCollection(collectionName: String): Flow<List<MeteWordItem>> {
        Log.d("MeteRepository", "Kelimeler sorgusu: $collectionName")
        return try {
            firestore.collection(collectionName)
                .orderBy("id", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MeteWordItem::class.java)
                }
                .catch { e ->
                    Log.e("MeteRepository", "Kelimeler sorgu hatası ($collectionName): ${e.message}")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Log.e("MeteRepository", "Kelimeler koleksiyon erişim hatası: $collectionName - ${e.message}")
            flowOf(emptyList())
        }
    }

    // Sağ panel hikayeleri/videoları için akış
    fun getStoriesFromCollection(collectionName: String): Flow<List<MeteStoryItem>> {
        Log.d("MeteRepository", "Hikayeler sorgusu: $collectionName")
        return try {
            firestore.collection(collectionName)
                .orderBy("id", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MeteStoryItem::class.java)
                }
                .catch { e ->
                    Log.e("MeteRepository", "Hikayeler sorgu hatası ($collectionName): ${e.message}")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Log.e("MeteRepository", "Hikayeler koleksiyon erişim hatası: $collectionName - ${e.message}")
            flowOf(emptyList())
        }
    }
}