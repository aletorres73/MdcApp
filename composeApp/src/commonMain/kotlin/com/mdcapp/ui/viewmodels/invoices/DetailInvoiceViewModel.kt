package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailInvoiceViewModel(
    invoiceNumber: String,
    private val getInvoiceUseCase: InvoiceUseCase.GetInvoiceByNumber,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(invoiceNumber = invoiceNumber))
    val state = _state.asStateFlow()

    data class UiState(
        val invoiceNumber: String = "",
        val isLoading: Boolean = false,
        val billing: BillingModel = BillingModel(),
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val error: String? = null
    )

    init {
        loadData(invoiceNumber)
    }

    /** SECUENCIA: Billing → BuyOrder */
    private fun loadData(invoiceNumber: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1️⃣ Obtener factura
            val billing = getInvoiceUseCase(invoiceNumber)
            if (billing.orderId.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No se encontró la factura"
                )
                return@launch
            }

            _state.value = _state.value.copy(billing = billing)
            Log.i("DetailInvoiceViewModel", "Billing: $billing")

            // 2️⃣ Obtener orden de compra usando el orderId ya seguro
            val orderId = billing.orderId
            if (orderId.isNotEmpty()) {
                val buyOrder = getBuyOrderUseCase(orderId)
                _state.value = _state.value.copy(buyOrder = buyOrder)
                Log.i("DetailInvoiceViewModel", "BuyOrder: $buyOrder")
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }
}
