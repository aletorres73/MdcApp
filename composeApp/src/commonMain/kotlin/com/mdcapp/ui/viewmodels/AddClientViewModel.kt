package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddClientViewModel(
    private val clientUseCase: GetClientsUseCase,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("AddClient")
    }

    data class UiState(
        val isLoading: Boolean = false,
        val suggestedId: String = "",
        val loadedClient: ClientModel? = null,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    fun loadNextId() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val nextId = clientUseCase.getNextId()
                _state.update { it.copy(isLoading = false, suggestedId = nextId) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadClientDetails(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val client = clientUseCase.getClientById(clientId)
                _state.update { it.copy(isLoading = false, loadedClient = client) }
            } catch (e: Exception) {
                Napier.e("Error loading client details", e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveClient(
        id: String,
        name: String,
        cuit: String = "",
        address: String = "",
        taxAddress: String = "",
        email: String = "",
        phone: String = "",
        city: String = "",
        isActive: Boolean = true
    ) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "El nombre / Razón Social no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val client = ClientModel(
                    clientId = id,
                    clientName = name.trim(),
                    cuit = cuit.trim(),
                    address = address.trim(),
                    taxAddress = taxAddress.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    city = city.trim(),
                    isActive = isActive
                )
                val success = clientUseCase.save(client)
                if (success) {
                    analytics.logEvent("add_client_success", mapOf("client_name" to name))
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent("add_client_failure", mapOf("reason" to "repository_error"))
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al guardar el cliente"
                        )
                    }
                }
            } catch (e: Exception) {
                Napier.e("Error adding client", e)
                analytics.logEvent("add_client_error", mapOf("error" to e.message))
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.update { UiState() }
    }
}
