package com.mdcapp.ui.viewmodels.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoicesPagedViewModel(
    private val getInvoicePaged: InvoiceUseCase.GetInvoicePaged
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState

    data class InvoiceUiState(
        val invoices: List<BillingModel> = emptyList(),
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
        )
    )

    init {
        val state = _uiState.value.selectedState
        _uiState.update { it.copy(cursor = getInvoicePaged.reset()) }
        loadFirstPage(state)
    }


    fun loadFirstPage(state: String) {
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
        val state = _uiState.value.selectedState
        val cursor = _uiState.value.cursor

        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true) }
            val (page, cursorResult) = getInvoicePaged.loadNextPage(20, state, cursor)
            _uiState.update {
                it.copy(
                    invoices = it.invoices + page.items.map { billing -> billing.toDomain() },
                    cursor = cursorResult,
                    isLoading = false,
                    endReached = page.items.isEmpty()
                )
            }
        }
    }
}
