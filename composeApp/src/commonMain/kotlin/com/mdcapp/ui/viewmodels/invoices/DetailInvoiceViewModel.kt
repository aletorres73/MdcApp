package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailInvoiceViewModel(
    invoiceNumber: String,
    private val getInvoiceUseCase: InvoiceUseCase.GetInvoiceByNumber
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(invoiceNumber = invoiceNumber))
    val state = _state.asStateFlow()

    data class UiState(
        val invoiceNumber: String = "",
        val isLoading: Boolean = false,
        val billing: BillingModel = BillingModel(),
        val error: String? = null
    )

    init {
        getBilling(invoiceNumber)
    }

    private fun getBilling(invoiceNumber: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = getInvoiceUseCase(invoiceNumber)
            if (result != BillingModel())
                _state.value = _state.value.copy(isLoading = false, billing = result)
            Log.i("DetailInvoiceViewModel", "result: $result")
        }
    }
}