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
    private val getClientNameUseCase: InvoiceUseCase.GetClientName,
    private val filterByBrandUseCase: InvoiceUseCase.FilterByBrand
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(clientId = clientId))
    val state = _state.asStateFlow()

    data class UiState(
        val clientId: String = "",
        val isLoading: Boolean = false,
        val client: ClientModel = ClientModel("", ""),
        val documents: List<BillingModel> = emptyList(),
        val brandList: List<String> = emptyList(),
        val error: String? = null
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

    init {
        Log.i("InvoicesViewModel", "clientId: $clientId")
        getClientName(clientId)
        getDocuments(clientId)
    }

    private fun launchWithState(block: suspend () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            block()
        } catch (e: Exception) {
            Log.e("InvoicesViewModel", "Error", e)
            _state.update { it.copy(isLoading = false, error = e.message ?: "Error desconocido") }
        }
    }

    private fun getClientName(clientId: String) = launchWithState {
        val client = getClientNameUseCase(clientId)
        _state.update { it.copy(client = client, isLoading = false) }
    }

    private fun getDocuments(clientId: String) = launchWithState {
        val documents = getDocumentsUseCase(clientId)

        // Generar lista de marcas únicas
        val brands = documents.asSequence()
            .map { it.brand }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        // Si hay al menos una marca, filtramos automáticamente por la primera
        if (brands.isNotEmpty()) {
            val firstBrand = brands.first()
            val filteredDocs = filterByBrandUseCase(firstBrand, clientId)
            val sorted = sortDocuments(filteredDocs)

            _state.update {
                it.copy(
                    isLoading = false,
                    documents = sorted,
                    brandList = brands
                )
            }
        } else {
            // Si no hay marcas, simplemente mostramos
            val sorted = sortDocuments(documents)
            _state.update {
                it.copy(
                    isLoading = false,
                    documents = sorted,
                    brandList = emptyList()
                )
            }
        }
    }

    fun filterByMarca(brandSelected: String) = launchWithState {
        val docs = filterByBrandUseCase(brandSelected, _state.value.clientId)
        val sorted = sortDocuments(docs)
        _state.update { it.copy(isLoading = false, documents = sorted) }
    }

    private fun sortDocuments(docs: List<BillingModel>) =
        docs.sortedBy { LocalDate.parse(it.loadDate, dateFormatter) }
}


