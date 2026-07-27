package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val clients: List<ClientModel> = emptyList(),
        val amountClients: Long = 0,
        val error: String? = null,
        val message: String? = null
    )

    init {
        loadClients()
    }

    fun loadClients() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val clients = getClientsUseCase.getAll()
                val amount = getClientsUseCase.getAmountClients()
                _state.update {
                    it.copy(
                        isLoading = false,
                        clients = clients,
                        amountClients = amount
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val success = getClientsUseCase.delete(clientId)
                if (success) {
                    _state.update { it.copy(message = "Cliente eliminado") }
                    loadClients()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error al eliminar") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
