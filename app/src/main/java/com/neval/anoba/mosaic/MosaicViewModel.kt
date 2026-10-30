package com.neval.anoba.mosaic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Suppress("Unused", "unused")
class MosaicViewModel(
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    companion object {
        private const val TOPLAM_KUTU_SAYISI = 240
        private const val VITRIN_ICERIK_SAYISI = 20
    }

    private val _mosaicItems = MutableStateFlow<List<MosaicItem>>(emptyList())
    val mosaicItems: StateFlow<List<MosaicItem>> = _mosaicItems

    private val _featuredItems = MutableStateFlow<List<MosaicItem>>(emptyList())
    val featuredItems: StateFlow<List<MosaicItem>> = _featuredItems

    private var mosaicListenerRegistration: ListenerRegistration? = null
    private var currentOrderIds = listOf<String>()

    init {
        observeMosaicItems()
    }

    private fun observeMosaicItems() {
        if (mosaicListenerRegistration != null) return // Mükerrer çağrıyı engelle

        Log.d("MosaicViewModel", "Mosaic dinleyicisi başlatıldı.")
        mosaicListenerRegistration = firestore.collection("mosaic_content")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MosaicViewModel", "VERİ ÇEKME HATASI: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("MosaicViewModel", "Snapshot geldi. Doküman sayısı: ${snapshot.size()}")
                    val allItems = mutableListOf<MosaicItem>()

                    for (doc in snapshot.documents) {
                        try {
                            doc.toObject(MosaicItem::class.java)?.let {
                                allItems.add(it.copy(id = doc.id))
                            }
                        } catch (e: Exception) {
                            Log.e("MosaicViewModel", "DÖNÜŞTÜRME HATASI (ID: ${doc.id}): ${e.message}")
                        }
                    }

                    Log.d("MosaicViewModel", "Başarıyla ayrıştırılan öğe sayısı: ${allItems.size}")
                    if (currentOrderIds.isEmpty() || (currentOrderIds.size != allItems.size)) {
                        processLists(allItems)
                    } else {
                        val currentItems = _mosaicItems.value
                        _mosaicItems.value = currentItems.map { oldItem ->
                            allItems.find { it.id == oldItem.id }?.copy(index = oldItem.index) ?: oldItem
                        }

                        val currentFeatured = _featuredItems.value
                        _featuredItems.value = currentFeatured.map { oldFeatured ->
                            allItems.find { it.id == oldFeatured.id } ?: oldFeatured
                        }
                    }
                }
            }
    }

    private fun processLists(allItems: List<MosaicItem>) {
        if (allItems.isEmpty()) return
        currentOrderIds = allItems.map { it.id }

        viewModelScope.launch(Dispatchers.Default) {
            // 1. Reklam alanlarını (17, 34, 51...) belirliyoruz
            val adIndices = (1 until TOPLAM_KUTU_SAYISI).asSequence().filter { (it % 17 == 0) }.toSet()

            // 2. Firebase'den gelen içerikleri kategorilere ayırıyoruz
            val adItems = allItems.filter { it.isAd || adIndices.contains(it.index.toInt()) }

            // Sabit içerikler: Index'i olan ama reklam olmayanlar
            val fixedItems = allItems.filter { it.index > 0 && !adIndices.contains(it.index.toInt()) && !it.isAd }
            val fixedIndices = fixedItems.asSequence().map { it.index.toInt() }.toSet()

            // Rastgele içerikler: Hiçbir gruba girmeyen tüm geriye kalanlar
            val randomItems = allItems.filter { !adItems.contains(it) && !fixedItems.contains(it) }

            // 3. Reklamları kendi içinde karıştırıp reklam alanlarına atıyoruz
            val shuffledAds = adItems.shuffled().asSequence().mapIndexed { i, item ->
                val targetIndex = adIndices.elementAtOrNull(i)?.toLong() ?: -1L
                item.copy(index = targetIndex)
            }.filter { it.index != -1L }.toList()

            // 4. Boşta kalan (reklam olmayan ve sabit olmayan) kutuları buluyoruz
            val usedIndices = fixedIndices + shuffledAds.map { it.index.toInt() }.toSet()
            val availableIndices = (0 until TOPLAM_KUTU_SAYISI)
                .filter { !usedIndices.contains(it) }
                .shuffled()

            // 5. Rastgele içerikleri bu boş kutulara dağıtıyoruz
            val assignedRandomItems = randomItems.mapIndexed { i, item ->
                val targetIndex = availableIndices.getOrElse(i) { -1 }.toLong()
                item.copy(index = targetIndex)
            }.filter { it.index != -1L }

            // 6. Hepsini birleştirip ekrana basıyoruz
            _mosaicItems.value = fixedItems + shuffledAds + assignedRandomItems

            val topFeatured = allItems.sortedByDescending { it.viewCount }
                .take(VITRIN_ICERIK_SAYISI)
                .shuffled()
            _featuredItems.value = topFeatured
        }
    }

    fun fetchMosaicItems() {
        if (currentOrderIds.isEmpty()) {
            observeMosaicItems()
        }
    }

    fun trackClick(itemId: String) {
        if (itemId.isEmpty()) return
        firestore.collection("mosaic_content").document(itemId)
            .update("clickCount", FieldValue.increment(1))
    }

    fun trackImpression(itemId: String) {
        if (itemId.isEmpty()) return
        firestore.collection("mosaic_content").document(itemId)
            .update("viewCount", FieldValue.increment(1))
    }

    override fun onCleared() {
        super.onCleared()
        mosaicListenerRegistration?.remove()
        mosaicListenerRegistration = null
    }
}
