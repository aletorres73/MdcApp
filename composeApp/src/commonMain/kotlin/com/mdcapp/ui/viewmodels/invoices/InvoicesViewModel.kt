package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.ClientModel
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoicesViewModel(
    clientId: String,
    private val getDocumentsUseCase: InvoiceUseCase.GetBillingsByClient,
    private val getClientNameUseCase: InvoiceUseCase.GetClientName
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val client: ClientModel = ClientModel("", ""),
        val documents: List<BillingModel> = emptyList(),
        val brandList: List<String> = emptyList(),
        val error: String? = null
    )

    init {
        Log.i("InvoicesViewModel", "clientId: $clientId")
        getDocuments(clientId)
        getClientName(clientId)
    }

    private fun getClientName(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val client = getClientNameUseCase(clientId)
                _state.update { it.copy(client = client, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun getDocuments(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val documents = getDocumentsUseCase(clientId)

                val formatter = DateTimeFormatter.ofPattern("d/MM/yyyy")
                val sorted = documents.sortedBy { LocalDate.parse(it.loadDate, formatter) }
                _state.update { it.copy(isLoading = false, documents = sorted) }
                getBrandList()

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun getBrandList() {
        val data = _state.value.documents
            .map { it.brand }
            .filter { it.isNotBlank() }
            .distinct()
        _state.update { it.copy(brandList = data) }
        Log.i("InvoicesViewModel", "brandList: $data")
    }


}
