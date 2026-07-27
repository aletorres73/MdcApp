package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.ClientModel
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoicesViewModel(
    clientId: String,
    private val observeDocumentsUseCase: InvoiceUseCase.ObserveBillingsByClient,
    private val getClientNameUseCase: InvoiceUseCase.GetClientName
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(clientId = clientId))
    val state = _state.asStateFlow()

    private val _selectedBrand = MutableStateFlow("")
    val selectedBrand = _selectedBrand.asStateFlow()

    fun setBrand(brand: String) {
        _selectedBrand.value = brand
    }

    data class UiState(
        val clientId: String = "",
        val isLoading: Boolean = false,
        val client: ClientModel = ClientModel("", ""),
        val documents: List<BillingModel> = emptyList(),
        val allDocuments: List<BillingModel> = emptyList(),
        val brandList: List<String> = emptyList(),
        val balance: Double = 0.0,
        val error: String? = null
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

    init {
        Log.i("InvoicesViewModel", "clientId: $clientId")
        getClientName(clientId)
        getDocuments(clientId)
    }

    //----------------------------------------------
    // HELPERS
    //----------------------------------------------

    private fun calculateBalance(documents: List<BillingModel>): Double {
        val total = documents.sumOf { it.rest }
        return total.toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }


    private fun sortDocuments(docs: List<BillingModel>) =
        docs.sortedBy { LocalDate.parse(it.loadDate, dateFormatter) }

    private fun launchWithState(block: suspend () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            block()
        } catch (e: Exception) {
            Log.e("InvoicesViewModel", "Error", e)
            _state.update { it.copy(isLoading = false, error = e.message ?: "Error desconocido") }
        }
    }

    //----------------------------------------------
    // LOAD CLIENT NAME
    //----------------------------------------------

    private fun getClientName(clientId: String) = launchWithState {
        val client = getClientNameUseCase(clientId)
        _state.update { it.copy(client = client, isLoading = false) }
    }

    //----------------------------------------------
    // LOAD DOCUMENTS + AUTO-FILTER BY FIRST BRAND
    //----------------------------------------------

    private fun getDocuments(clientId: String) {
        combine(
            observeDocumentsUseCase(clientId),
            _selectedBrand
        ) { documents, selectedBrand ->
            val brands = documents.asSequence()
                .map { it.brand }
                .filter { it.isNotBlank() }
                .distinct()
                .toList()

            val activeBrand = if (selectedBrand.isEmpty() && brands.isNotEmpty()) {
                setBrand(brands.first())
                brands.first()
            } else selectedBrand

            val filteredDocs = if (activeBrand.isNotEmpty()) {
                documents.filter { it.brand == activeBrand }
            } else documents

            val sorted = sortDocuments(filteredDocs)
            val balance = calculateBalance(sorted)

            _state.update {
                it.copy(
                    isLoading = false,
                    documents = sorted,
                    allDocuments = documents,
                    brandList = brands,
                    balance = balance
                )
            }
        }.launchIn(viewModelScope)
    }

    //----------------------------------------------
    // FILTER BY BRAND
    //----------------------------------------------

    fun filterByMarca() {
        // Al usar combine, esto se actualiza automáticamente al llamar a setBrand
    }
}


