package com.neval.anoba.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedHomeViewModel(
    private val repository: SharedHomeRepository = SharedHomeRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _feedItems = MutableStateFlow<List<SharedFeedItem>>(emptyList())
    val feedItems: StateFlow<List<SharedFeedItem>> = _feedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        scope.launch {
            repository.getCombinedHomeFeed().collect { items ->
                _feedItems.value = items
                _isLoading.value = false
            }
        }
    }
}
