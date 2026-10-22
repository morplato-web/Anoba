package com.neval.anoba.market

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface MarketCreateUiState {
    data object Idle : MarketCreateUiState
    data object Loading : MarketCreateUiState
    data object Success : MarketCreateUiState
    data class Error(val message: String) : MarketCreateUiState
}

class MarketViewModel(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _createUiState = MutableStateFlow<MarketCreateUiState>(MarketCreateUiState.Idle)
    val createUiState: StateFlow<MarketCreateUiState> = _createUiState.asStateFlow()

    val categories = listOf(
        "Tümü",
        "Elektronik",
        "Giyim",
        "Ev & Yaşam",
        "Kitap & Hobi",
        "El Yapımı",
        "Diğer"
    )

    val items: StateFlow<List<MarketItemModel>> = combine(
        marketRepository.getMarketItemsStream(),
        _selectedCategory,
        _searchQuery
    ) { allItems, category, query ->
        allItems.filter { item ->
            val matchesCategory = (category == "Tümü" || item.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.city.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            marketRepository.syncMarketItemsFromRemote()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createMarketItem(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        priceText: String,
        category: String,
        city: String,
        condition: String
    ) {
        if (title.isBlank()) {
            _createUiState.value = MarketCreateUiState.Error("Lütfen ilan başlığı giriniz")
            return
        }
        val price = priceText.toDoubleOrNull() ?: 0.0
        if (price <= 0.0) {
            _createUiState.value = MarketCreateUiState.Error("Lütfen geçerli bir fiyat giriniz")
            return
        }
        if (city.isBlank()) {
            _createUiState.value = MarketCreateUiState.Error("Lütfen şehir bilgisi giriniz")
            return
        }

        _createUiState.value = MarketCreateUiState.Loading
        viewModelScope.launch {
            val result = marketRepository.createMarketItem(
                context = context,
                imageUri = imageUri,
                title = title,
                description = description,
                price = price,
                category = category,
                city = city,
                condition = condition
            )
            result.onSuccess {
                _createUiState.value = MarketCreateUiState.Success
            }.onFailure { error ->
                _createUiState.value = MarketCreateUiState.Error(error.message ?: "İlan eklenirken bir hata oluştu")
            }
        }
    }

    fun resetCreateState() {
        _createUiState.value = MarketCreateUiState.Idle
    }

    fun markAsSold(itemId: String) {
        viewModelScope.launch {
            marketRepository.markAsSold(itemId)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            marketRepository.deleteItem(itemId)
        }
    }
}
