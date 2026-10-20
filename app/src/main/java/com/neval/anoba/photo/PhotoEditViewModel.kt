package com.neval.anoba.photo

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class PhotoEditState {
    object Loading : PhotoEditState()
    data class Success(val photo: PhotoModel) : PhotoEditState()
    data class Error(val message: String) : PhotoEditState()
}

class PhotoEditViewModel(
    private val photoId: String,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _photoState = mutableStateOf<PhotoEditState>(PhotoEditState.Loading)
    val photoState: State<PhotoEditState> = _photoState

    init {
        loadPhoto()
    }

    private fun loadPhoto() {
        viewModelScope.launch {
            val photo = photoRepository.getPhoto(photoId).firstOrNull()
            if (photo != null) {
                _photoState.value = PhotoEditState.Success(photo)
            } else {
                _photoState.value = PhotoEditState.Error("Fotoğraf bulunamadı.")
            }
        }
    }

    fun updatePhotoDetails(title: String, personalNote: String) {
        viewModelScope.launch {
            val currentState = _photoState.value
            if (currentState is PhotoEditState.Success) {
                photoRepository.updatePhotoDetails(currentState.photo.id, title, personalNote)
            }
        }
    }
}