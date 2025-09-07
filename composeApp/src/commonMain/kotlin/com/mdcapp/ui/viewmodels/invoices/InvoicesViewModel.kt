package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoicesViewModel(
    private val getDocumentsUseCase: InvoiceUseCase.GetBillingsByClient
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val documents: List<BillingModel> = emptyList(),
        val error: String? = null
    )


    fun init(clientId: String) {
        Log.i("InvoicesViewModel", "clientId: $clientId")
        getDocuments(clientId)
    }

    private fun getDocuments(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val documents = getDocumentsUseCase(clientId)
                _state.update { it.copy(isLoading = false, documents = documents) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}
