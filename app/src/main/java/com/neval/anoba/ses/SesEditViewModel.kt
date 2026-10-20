package com.neval.anoba.ses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SesEditState {
    data object Loading : SesEditState()
    data class Success(val ses: SesModel) : SesEditState()
    data object UpdateSuccess : SesEditState()
    data class Error(val message: String) : SesEditState()
}

class SesEditViewModel(
    private val sesId: String,
    private val sesRepository: SesRepository
) : ViewModel() {

    private val _sesState = MutableStateFlow<SesEditState>(SesEditState.Loading)
    val sesState: StateFlow<SesEditState> = _sesState.asStateFlow()

    init {
        loadSes()
    }

    private fun loadSes() {
        viewModelScope.launch {
            _sesState.value = SesEditState.Loading
            try {
                val ses = sesRepository.getSes(sesId)
                if (ses != null) {
                    _sesState.value = SesEditState.Success(ses)
                } else {
                    _sesState.value = SesEditState.Error("Ses kaydı bulunamadı.")
                }
            } catch (e: Exception) {
                _sesState.value = SesEditState.Error("Ses kaydı yüklenirken bir hata oluştu: ${e.message}")
            }
        }
    }

    fun updateSesDetails(
        title: String,
        personalNote: String,
        trimStartMs: Long?,
        trimEndMs: Long?
    ) {
        viewModelScope.launch {
            try {
                val currentSes = (_sesState.value as? SesEditState.Success)?.ses ?: return@launch

                // Calculate new duration based on trimming
                val originalDuration = currentSes.duration
                val effectiveStart = trimStartMs ?: 0L
                val effectiveEnd = trimEndMs ?: originalDuration
                val newDuration = effectiveEnd - effectiveStart

                val updates = mutableMapOf<String, Any?>(
                    "title" to title,
                    "personalNote" to personalNote,
                    "duration" to newDuration, // Always update duration
                    "trimStartMs" to trimStartMs,
                    "trimEndMs" to trimEndMs
                )

                sesRepository.updateSes(sesId, updates)
                _sesState.value = SesEditState.UpdateSuccess
            } catch (e: Exception) {
                _sesState.value = SesEditState.Error("Güncelleme sırasında hata: ${e.message}")
            }
        }
    }
}
