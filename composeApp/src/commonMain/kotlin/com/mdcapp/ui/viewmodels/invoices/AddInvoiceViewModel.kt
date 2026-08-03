package com.mdcapp.ui.viewmodels.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingComments
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.recalculate
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInvoiceViewModel(
    orderId: String,
    private val createInvoiceUseCase: InvoiceUseCase.CreateInvoice,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(orderId = orderId))
    val state = _state.asStateFlow()

    init {
        analytics.logScreenView("AddInvoice")
    }

    data class UiState(
        val orderId: String = "",
        val isLoading: Boolean = false,
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val paymentConditionList: List<PaymentCondition> = emptyList(),
        val selectedCondition: PaymentCondition? = null,
        val isSuccess: Boolean = false,
        val error: String? = null
    )

    fun loadData(clientId: String, orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val buyOrder = getBuyOrderUseCase(clientId, orderId)
                val conditions = getPaymentConditionUseCase(buyOrder.branch, buyOrder.factory)

                val inheritedCondition = if (buyOrder.paymentCondition.isNotEmpty()) {
                    PaymentCondition(
                        paymentName = buyOrder.paymentCondition,
                        discount = buyOrder.discount,
                        expiration = buyOrder.expirationDays
                    )
                } else null

                _state.update {
                    it.copy(
                        isLoading = false,
                        buyOrder = buyOrder,
                        paymentConditionList = conditions,
                        selectedCondition = inheritedCondition
                    )
                }
            } catch (e: Exception) {
                Napier.e("Error loading data for AddInvoice", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun saveInvoice(
        number: String,
        amount: Double,
        condition: PaymentCondition?,
        deliveryDate: Long,
        payDate: Long,
        type: String,
        notes: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val now = System.currentTimeMillis()

                val comments = if (notes.isNotBlank()) {
                    listOf(BillingComments(notes, now))
                } else emptyList()

                val baseInvoice = BillingModel(
                    billingNumber = number,
                    orderId = _state.value.orderId,
                    total = amount,
                    brand = _state.value.buyOrder.factory,
                    branch = _state.value.buyOrder.branch,
                    clientId = _state.value.buyOrder.clientId,
                    clientName = _state.value.buyOrder.client,
                    payDate = payDate,
                    deliveryDate = deliveryDate,
                    paymentCondition = condition?.paymentName ?: "",
                    expectedDiscount = condition?.discount ?: 0.0,
                    type = type,
                    comments = comments,
                    stateBilling = "Pendiente",
                    loadDate = now,
                    timeStamp = System.currentTimeMillis()
                )

                val finalInvoice =
                    if (condition != null) baseInvoice.recalculate(condition) else baseInvoice

                val success = createInvoiceUseCase(
                    _state.value.buyOrder.clientId,
                    _state.value.orderId,
                    finalInvoice
                )
                if (success) {
                    analytics.logEvent(
                        "add_invoice_success", mapOf(
                            "invoice_number" to number,
                            "amount" to amount,
                            "client" to finalInvoice.clientName
                        )
                    )
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    analytics.logEvent("add_invoice_failure", mapOf("reason" to "repository_error"))
                    _state.update { it.copy(isLoading = false, error = "Error al guardar factura") }
                }
            } catch (e: Exception) {
                Napier.e("Error saving invoice", e)
                analytics.logEvent("add_invoice_error", mapOf("error" to e.message))
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

