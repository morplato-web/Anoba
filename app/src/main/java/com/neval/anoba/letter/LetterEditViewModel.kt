package com.neval.anoba.letter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LetterEditViewModel(
    private val letterId: String,
    private val letterRepository: LetterRepository
) : ViewModel() {

    private val _letterState = MutableStateFlow<LetterEditState>(LetterEditState.Loading)
    val letterState: StateFlow<LetterEditState> = _letterState.asStateFlow()

    init {
        loadLetter()
    }

    private fun loadLetter() {
        viewModelScope.launch {
            _letterState.value = LetterEditState.Loading
            try {
                val letter = letterRepository.getLetter(letterId)
                if (letter != null) {
                    _letterState.value = LetterEditState.Success(letter)
                } else {
                    _letterState.value = LetterEditState.Error("Mektup bulunamadı.")
                }
            } catch (e: Exception) {
                _letterState.value = LetterEditState.Error("Mektup yüklenirken bir hata oluştu: ${e.message}")
            }
        }
    }

    fun updateLetterDetails(title: String, content: String, personalNote: String, pseudonym: String, isAskAnoba: Boolean) {
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "title" to title,
                    "content" to content,
                    "personalNote" to personalNote,
                    "pseudonym" to pseudonym,
                    "is_ask_anoba" to isAskAnoba
                )
                
                // Eğer Anoba Ablası'na soruluyorsa gizliliği otomatik Halka Açık (PUBLIC) yapalım
                if (isAskAnoba) {
                    updates["privacy"] = "PUBLIC"
                }

                letterRepository.updateLetter(letterId, updates)
            } catch (_: Exception) {
                // Update error can be handled here if needed
            }
        }
    }
}