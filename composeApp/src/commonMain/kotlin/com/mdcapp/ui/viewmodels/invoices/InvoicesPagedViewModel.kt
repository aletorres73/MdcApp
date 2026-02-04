package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.ui.composables.invoicesPage.TypeSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoicesPagedViewModel(
    private val getInvoicePaged: InvoiceUseCase.GetInvoicePaged,
    private val getClients: InvoiceUseCase.GetAllClients
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState

    data class InvoiceUiState(
        val invoices: List<BillingModel> = emptyList(),
        val clientNameList: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val selectedState: String = "Vencido",
        val cursor: String? = null,
        val endReached: Boolean = false,
        val availableStates: List<String> = listOf(
            "Vencido",
            "A cobrar",
            "Por vencer",
            "Cobrado",
            "Sin información"
        ),
        val clientSearch: String? = null,
        val numberSearch: String? = null,
        val typeSearch: TypeSearch? = TypeSearch.Client
    )

    init {
        _uiState.update { it.copy(isLoading = true) }
        val state = _uiState.value.selectedState
        loadAllClients()
        loadFirstPage(state)
    }

    private fun loadAllClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(clientNameList = getClients()) }
            Log.i("InvoicesPagedViewModel", "Clients: ${_uiState.value.clientNameList}")
        }
    }

    fun stateSelected(state: String) {
        _uiState.update {
            it.copy(
                selectedState = state,
                endReached = false
            )
        }
        reload()
    }

    private fun loadFirstPage(state: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    invoices = emptyList(),
                    selectedState = state
                )
            }
            val (page, cursor) = getInvoicePaged.loadNextPage(20, state)
            _uiState.update {
                it.copy(
                    invoices = page.items.map { billing -> billing.toDomain() },
                    cursor = cursor,
                    isLoading = false,
                    endReached = page.items.isEmpty()
                )
            }
        }
    }

    fun loadNextPage() {
        val current = _uiState.value
        if (current.isLoading || current.endReached) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val (page, cursor) = getInvoicePaged.loadNextPage(
                limit = 20,
                state = current.selectedState,
                cursor = current.cursor,
                client = current.clientSearch,
                number = current.numberSearch
            )

            _uiState.update {
                it.copy(
                    invoices = it.invoices + page.items.map { it.toDomain() },
                    cursor = cursor,
                    isLoading = false,
                    endReached = page.items.isEmpty()
                )
            }
        }
    }

    private fun reload() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    invoices = emptyList(),
                    cursor = getInvoicePaged.reset(),
                    endReached = false
                )
            }

            val state = _uiState.value.selectedState
            val client = _uiState.value.clientSearch
            val number = _uiState.value.numberSearch

            val (page, cursor) = getInvoicePaged.loadNextPage(
                limit = 20,
                state = state,
                cursor = null,
                client = client,
                number = number
            )

            _uiState.update {
                it.copy(
                    invoices = page.items.map { it.toDomain() },
                    cursor = cursor,
                    isLoading = false,
                    endReached = page.items.isEmpty()
                )
            }
        }
    }


    fun onSelectedTypeSearch(type: TypeSearch) {
        _uiState.update { it.copy(typeSearch = type) }
    }

    fun onSearch(query: String) {
        when (_uiState.value.typeSearch) {
            TypeSearch.Client -> {
                _uiState.update {
                    it.copy(clientSearch = query, numberSearch = null)
                }
            }

            TypeSearch.Number -> {
                _uiState.update {
                    it.copy(numberSearch = query, clientSearch = null)
                }
            }

            null -> return
        }
        reload()
    }

    fun clearQuery() {
        _uiState.update { it.copy(clientSearch = null, numberSearch = null) }
        reload()
    }

}
