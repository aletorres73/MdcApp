package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
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

class DetailInvoiceViewModel(
    invoiceNumber: String,
    private val getInvoiceUseCase: InvoiceUseCase.GetInvoiceByNumber,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(invoiceNumber = invoiceNumber))
    val state = _state.asStateFlow()

    data class UiState(
        val invoiceNumber: String = "",
        val isLoading: Boolean = false,
        val billing: BillingModel = BillingModel(),
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val error: String? = null,
        val paymentConditionList: List<PaymentCondition> = emptyList(),
        val message: String? = null
    )

    init {
        loadData(invoiceNumber)
    }

    /** SECUENCIA: Billing → BuyOrder */
    private fun loadData(invoiceNumber: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val billing = getInvoiceUseCase(invoiceNumber)
                if (billing.orderId.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró la factura"
                        )
                    }
                    return@launch
                }

                val buyOrder = if (billing.orderId.isNotEmpty()) {
                    getBuyOrderUseCase(billing.clientId, billing.orderId)
                } else BuyOrderModel()
// ...

                val paymentConditions = getPaymentConditionUseCase(billing.brand)

                _state.update {
                    it.copy(
                        billing = billing.recalculate(),
                        buyOrder = buyOrder,
                        paymentConditionList = paymentConditions,
                        isLoading = false
                    )
                }
                Log.i("MdcAppOnly", "DetailInvoiceViewModel --- on loadData: $billing")

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
                Log.e("MdcAppOnly", "DetailInvoiceViewModel --- on loadData: $e")
            }
        }
    }

    fun updateSelectedPaymentCondition(condition: PaymentCondition) {
        viewModelScope.launch {
            val current = _state.value.billing

            val updated = current.copy(
                discount = condition.discount,
                paymentCondition = condition.paymentName
            ).recalculate(condition)

            _state.update {
                it.copy(billing = updated)
            }

            saveBilling()
        }
    }


    fun updateDeliveryDate(newDate: String) {
        val current = _state.value

        val condition = current.paymentConditionList
            .find { it.paymentName == current.billing.paymentCondition }
            ?: return

        val updated = current.billing
            .copy(deliveryDate = newDate)
            .recalculate(condition)

        _state.update {
            it.copy(billing = updated)
        }

        saveBilling()

        Log.i("MdcAppOnly", "DetailInvoiceViewModel --- on updateDeliveryDate: $updated")
    }

    fun registerPayment(amount: Double) {
        val current = _state.value
        val updated = current.billing.copy(
            payed = current.billing.payed + amount
        ).recalculate()

        _state.update {
            it.copy(billing = updated)
        }

        saveBilling()
    }

    @androidx.annotation.RequiresApi(26)
    fun addComment(text: String) {
        val now = try {
            val date = java.time.LocalDate.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            date.format(formatter)
        } catch (e: Exception) {
            ""
        }

        val newComment = com.mdcapp.data.model.BillingComments(text, now)
        val current = _state.value
        val updated = current.billing.copy(
            comments = current.billing.comments + newComment
        )

        _state.update { it.copy(billing = updated) }
        saveBilling()
    }

    fun updateState(newState: String) {
        val current = _state.value
        val updated = current.billing.copy(stateBilling = newState)
        _state.update { it.copy(billing = updated) }
        saveBilling()
    }

    private fun saveBilling() {
        viewModelScope.launch {
            val billing = _state.value.billing
            val result = updateInvoiceUseCase(
                billing.clientId,
                billing.orderId,
                billing.billingNumber,
                billing
            )

            _state.update {
                it.copy(
                    message = if (result) "Actualizado" else "Error al guardar"
                )
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
