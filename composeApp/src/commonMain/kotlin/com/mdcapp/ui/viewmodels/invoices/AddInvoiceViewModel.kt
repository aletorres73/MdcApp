package com.mdcapp.ui.viewmodels.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.recalculate
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInvoiceViewModel(
    orderId: String,
    private val createInvoiceUseCase: InvoiceUseCase.CreateInvoice,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(orderId = orderId))
    val state = _state.asStateFlow()

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
                val conditions = getPaymentConditionUseCase(buyOrder.branch)

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
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    @androidx.annotation.RequiresApi(26)
    fun saveInvoice(
        number: String,
        amount: Double,
        condition: PaymentCondition?,
        deliveryDate: String,
        payDate: String,
        type: String,
        notes: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val now = try {
                    val date = java.time.LocalDate.now()
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    date.format(formatter)
                } catch (e: Exception) {
                    ""
                }

                val comments = if (notes.isNotBlank()) {
                    listOf(com.mdcapp.data.model.BillingComments(notes, now))
                } else emptyList()

                val baseInvoice = BillingModel(
                    billingNumber = number,
                    orderId = _state.value.orderId,
                    total = amount,
                    brand = _state.value.buyOrder.branch,
                    clientId = _state.value.buyOrder.clientId,
                    clientName = _state.value.buyOrder.client,
                    payDate = payDate,
                    deliveryDate = deliveryDate,
                    paymentCondition = condition?.paymentName ?: "",
                    discount = condition?.discount ?: 0.0,
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
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error al guardar factura") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
