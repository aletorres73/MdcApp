package com.mdcapp.ui.viewmodels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val getClientsUseCase: GetClientsUseCase,
    refreshController: com.mdcapp.domain.service.RefreshController,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery = _searchQuery

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    val state: StateFlow<UiState> = combine(
        getClientsUseCase.observeAll(),
        _searchQuery,
        _isSearchMode,
        combine(_isLoading, _error, _message) { loading, err, msg ->
            Triple(loading, err, msg)
        }
    ) { clients, query, isSearch, status ->
        val (loading, err, msg) = status

        val filteredClients = if (isSearch && query.text.isNotBlank()) {
            val searchTerm = query.text.lowercase().trim()
            clients.filter {
                it.clientName.lowercase().contains(searchTerm) ||
                        it.clientId.lowercase().contains(searchTerm)
            }
        } else {
            clients
        }

        UiState(
            isLoading = loading,
            clients = filteredClients.sortedBy { it.clientName },
            amountClients = clients.size.toLong(),
            error = err,
            message = msg,
            searchQuery = query,
            isSearchMode = isSearch
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(isLoading = true)
    )

    data class UiState(
        val isLoading: Boolean = false,
        val clients: List<ClientModel> = emptyList(),
        val amountClients: Long = 0,
        val error: String? = null,
        val message: String? = null,
        val searchQuery: TextFieldValue = TextFieldValue(""),
        val isSearchMode: Boolean = false
    )

    init {
        analytics.logScreenView("Clients")
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = TextFieldValue(query)
    }

    fun setSearchMode(enabled: Boolean) {
        _isSearchMode.value = enabled
        if (!enabled) {
            _searchQuery.value = TextFieldValue("")
        }
    }

    fun clearQuery() {
        _searchQuery.value = TextFieldValue("")
    }

    fun loadClients() {
        // Obsoleto con Flow, pero lo mantenemos si el refreshController lo llama
        // o podemos simplemente disparar un refresh en el repositorio si fuera necesario.
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = getClientsUseCase.delete(clientId)
                if (success) {
                    analytics.logEvent("delete_client_success", mapOf("client_id" to clientId))
                    _message.value = "Cliente eliminado"
                    _isLoading.value = false
                    // El flujo se encargará de actualizar la lista automáticamente
                } else {
                    _isLoading.value = false
                    _error.value = "Error al eliminar"
                }
            } catch (e: Exception) {
                Napier.e("Error deleting client", e)
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

