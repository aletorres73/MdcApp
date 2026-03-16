package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailInvoiceViewModel(
    invoiceNumber: String,
    private val getInvoiceUseCase: InvoiceUseCase.GetInvoiceByNumber,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(invoiceNumber = invoiceNumber))
    val state = _state.asStateFlow()

    data class UiState(
        val invoiceNumber: String = "",
        val isLoading: Boolean = false,
        val billing: BillingModel = BillingModel(),
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val error: String? = null,
        val paymentConditionList: List<PaymentCondition> = emptyList()
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
            Log.i("MdcAppOnly", "DetailInvoiceViewModel --- Billing: $billing")

            getPaymentCondition(billing.brand)

            // 2️⃣ Obtener orden de compra usando el orderId ya seguro
            val orderId = billing.orderId
            if (orderId.isNotEmpty()) {
                val buyOrder = getBuyOrderUseCase(orderId)
                _state.value = _state.value.copy(buyOrder = buyOrder)
                Log.i("DetailInvoiceViewModel", "BuyOrder: $buyOrder")
            }
            val toPay = billing.total - billing.discount * billing.total
            val rest = toPay - billing.payed

            _state.value = _state.value.copy(
                isLoading = false,
                billing = billing.copy(
                    toPay = toPay,
                    rest = rest
                )
            )
        }
    }

    private fun getPaymentCondition(brand: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val result = getPaymentConditionUseCase(brand)
                if (result.isNotEmpty())
                    _state.update { it.copy(paymentConditionList = result, isLoading = false) }
                else {
                    _state.update {
                        it.copy(
                            error = "No se encontraron condiciones de pago",
                            isLoading = false
                        )
                    }
                }
                Log.i("MdcAppOnly", "DetailInvoiceViewModel --- Result: $result")

            } catch (e: Exception) {
                Log.e("MdcAppOnly", "DetailInvoiceViewModel --- on getPaymentCondition: $e")
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateSelectedPaymentCondition(condition: PaymentCondition = PaymentCondition()) {
        val billing = _state.value.billing
        val discount = condition.discount
        val toPay = billing.total - discount * billing.total
        val rest = toPay - billing.payed

        Log.i(
            "MdcAppOnly",
            "DetailInvoiceViewModel --- on updateSelectedPaymentCondition: $condition"
        )

        _state.update {
            it.copy(
                billing = billing.copy(
                    discount = discount,
                    toPay = toPay,
                    rest = rest,
                    paymentCondition = condition.paymentName
                )
            )
        }
//        _state.update { it.copy(billing = it.billing.copy(paymentCondition = condition.paymentName)) }
    }
}
