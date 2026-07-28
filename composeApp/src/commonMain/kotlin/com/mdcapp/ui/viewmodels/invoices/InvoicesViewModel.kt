package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.entities.recalculate
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
    private val getClientNameUseCase: InvoiceUseCase.GetClientName,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(clientId = clientId))
    val state = _state.asStateFlow()

    private val _selectedBrand = MutableStateFlow("Todas")
    val selectedBrand = _selectedBrand.asStateFlow()

    private val _selectedBranch = MutableStateFlow("Todas")
    val selectedBranch = _selectedBranch.asStateFlow()

    private val _selectedType = MutableStateFlow("Todos")
    val selectedType = _selectedType.asStateFlow()

    fun setBrand(brand: String) {
        _selectedBrand.value = brand
        _selectedBranch.value = "Todas" // Reset branch when brand changes
    }

    fun setBranch(branch: String) {
        _selectedBranch.value = branch
    }

    fun setType(type: String) {
        _selectedType.value = type
    }

    data class UiState(
        val clientId: String = "",
        val isLoading: Boolean = false,
        val client: ClientModel = ClientModel("", ""),
        val documents: List<BillingModel> = emptyList(),
        val allDocuments: List<BillingModel> = emptyList(),
        val brandList: List<String> = emptyList(),
        val branchList: List<String> = emptyList(),
        val typeList: List<String> = emptyList(),
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
            _selectedBrand,
            _selectedBranch,
            _selectedType
        ) { documents, selectedBrand, selectedBranch, selectedType ->
            // Extract available brands
            val brands = (listOf("Todas") + documents.asSequence()
                .map { it.brand }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .toList())

            // Filter by Brand
            val filteredByBrand = if (selectedBrand != "Todas" && selectedBrand.isNotEmpty()) {
                documents.filter { it.brand == selectedBrand }
            } else {
                documents
            }

            // Extract available branches for the currently filtered set
            val branches = (listOf("Todas") + filteredByBrand.asSequence()
                .map { it.branch }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .toList())

            // Filter by Branch
            val filteredByBranch = if (selectedBranch != "Todas" && selectedBranch.isNotEmpty()) {
                filteredByBrand.filter { it.branch == selectedBranch }
            } else {
                filteredByBrand
            }

            // Extract available types
            val types = (listOf("Todos") + documents.asSequence()
                .map { it.type }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .toList())

            // Filter by Type
            val filteredDocs = if (selectedType != "Todos" && selectedType.isNotEmpty()) {
                filteredByBranch.filter { it.type == selectedType }
            } else {
                filteredByBranch
            }

            val recalculatedDocs = filteredDocs.map { billing ->
                val updated = billing.recalculate()
                if (updated.stateBilling != billing.stateBilling) {
                    viewModelScope.launch {
                        updateInvoiceUseCase(
                            updated.clientId,
                            updated.orderId,
                            updated.billingNumber,
                            updated
                        )
                    }
                }
                updated
            }
            val sorted = sortDocuments(recalculatedDocs)
            val balance = calculateBalance(sorted)

            _state.update {
                it.copy(
                    isLoading = false,
                    documents = sorted,
                    allDocuments = documents,
                    brandList = brands,
                    branchList = if (selectedBrand != "Todas" && selectedBrand.isNotEmpty()) branches else emptyList(),
                    typeList = types,
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



