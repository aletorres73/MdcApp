package com.mdcapp.ui.viewmodels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ClientsViewModel(
    private val getClientsUseCase: GetClientsUseCase,
    refreshController: com.mdcapp.domain.service.RefreshController,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery = _searchQuery

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode

    private val _showInactive = MutableStateFlow(false)
    val showInactive = _showInactive

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    private val debouncedQuery = _searchQuery
        .map { it.text }
        .distinctUntilChanged()
        .debounce { if (it.isEmpty()) 0 else 300 }

    private data class Status(
        val isLoading: Boolean,
        val error: String?,
        val message: String?,
        val isSearchMode: Boolean,
        val showInactive: Boolean
    )

    private val status = combine(
        _isLoading, _error, _message, _isSearchMode, _showInactive
    ) { loading, err, msg, isSearch, showInactive ->
        Status(loading, err, msg, isSearch, showInactive)
    }

    val state: StateFlow<UiState> = combine(
        getClientsUseCase.observeAll(),
        _searchQuery,
        debouncedQuery,
        status
    ) { clients, rawQuery, query, status ->
        val filteredClients = clients.filter {
            val matchesSearch = if (!status.isSearchMode || query.isBlank()) {
                true
            } else {
                it.clientName.lowercase().contains(query.lowercase().trim()) ||
                        it.clientId.lowercase().contains(query.lowercase().trim())
            }

            (it.isActive || status.showInactive) && matchesSearch
        }

        UiState(
            isLoading = status.isLoading,
            clients = filteredClients.sortedBy { it.clientName },
            amountClients = clients.filter { it.isActive }.size.toLong(),
            error = status.error,
            message = status.message,
            searchQuery = rawQuery,
            isSearchMode = status.isSearchMode,
            showInactive = status.showInactive
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
        val isSearchMode: Boolean = false,
        val showInactive: Boolean = false
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
        viewModelScope.launch {
            _isLoading.value = true
            // En modo reactivo no solemos llamar a nada, pero aquí podemos forzar un delay
            // o llamar a un refresh en el repositorio si IDatabaseRepository lo soportara.
            kotlinx.coroutines.delay(800)
            _isLoading.value = false
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = getClientsUseCase.delete(clientId)
                if (success) {
                    analytics.logEvent("archive_client_success", mapOf("client_id" to clientId))
                    _message.value = "Cliente archivado"
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _error.value = "Error al archivar"
                }
            } catch (e: Exception) {
                Napier.e("Error archiving client", e)
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun restoreClient(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = getClientsUseCase.restore(clientId)
                if (success) {
                    analytics.logEvent("restore_client_success", mapOf("client_id" to clientId))
                    _message.value = "Cliente restaurado"
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _error.value = "Error al restaurar"
                }
            } catch (e: Exception) {
                Napier.e("Error restoring client", e)
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun toggleShowInactive() {
        _showInactive.value = !_showInactive.value
    }

    fun clearMessage() {
        _message.value = null
    }
}

