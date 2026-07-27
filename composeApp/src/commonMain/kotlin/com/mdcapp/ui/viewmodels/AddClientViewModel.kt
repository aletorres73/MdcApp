package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddClientViewModel(private val clientUseCase: GetClientsUseCase) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    fun saveClient(name: String, id: String) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val success = clientUseCase.save(ClientModel(clientId = id, clientName = name))
            if (success) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Error al guardar el cliente") }
            }
        }
    }

    fun resetState() {
        _state.update { UiState() }
    }
}

