package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.service.UserService
import com.mdcapp.domain.entities.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val userService: UserService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userService.observeUserProfile().collectLatest { profile ->
                _uiState.value = _uiState.value.copy(
                    userProfile = profile,
                    isLoading = false
                )
            }
        }
    }

    fun uploadReceipt(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true)
            try {
                userService.uploadReceipt(imageBytes)
                // Ya no hace falta recargar manualmente porque observeUserProfile() lo hace solo
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = e.message
                )
            }
        }
    }

    fun resetUploadState() {
        _uiState.value = _uiState.value.copy(uploadSuccess = false, error = null)
    }
}

data class SubscriptionUiState(
    val userProfile: UserModel? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)
