package com.mdcapp.ui.viewmodels.invoices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.PaymentRegisterModel
import com.mdcapp.data.model.recalculate
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailInvoiceViewModel(
    invoiceNumber: String,
    private val observeInvoiceUseCase: InvoiceUseCase.ObserveInvoice,
    private val getBuyOrderUseCase: BuyOrderUseCase.GetBuyOrderById,
    private val getPaymentConditionUseCase: InvoiceUseCase.GetPaymentCondition,
    private val updateInvoiceUseCase: InvoiceUseCase.UpdateInvoice,
    private val observePaymentsRegisterUseCase: InvoiceUseCase.ObservePaymentsByInvoice,
    private val addPaymentToRegisterUseCase: BuyOrderUseCase.AddPaymentToRegister,
    private val getLastIdPaymentUseCase: BuyOrderUseCase.GetLastIdPaymentFromRegister
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
        val message: String? = null,
        val payments: List<PaymentRegisterModel> = emptyList()
    )

    init {
        loadData(invoiceNumber)
    }

    /** SECUENCIA: Billing → BuyOrder */
    private fun loadData(invoiceNumber: String) {
        observeInvoiceUseCase(invoiceNumber)
            .onEach { billing ->
                if (billing.orderId.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró la factura"
                        )
                    }
                    return@onEach
                }

                // Cargar BuyOrder solo si cambia o si aún no se tiene
                if (_state.value.buyOrder.id != billing.orderId) {
                    viewModelScope.launch {
                        try {
                            val buyOrder = getBuyOrderUseCase(billing.clientId, billing.orderId)
                            _state.update { it.copy(buyOrder = buyOrder) }
                        } catch (e: Exception) {
                            Log.e("MdcAppOnly", "Error loading BuyOrder: $e")
                        }
                    }
                }

                // Cargar condiciones solo una vez
                if (_state.value.paymentConditionList.isEmpty()) {
                    viewModelScope.launch {
                        try {
                            val paymentConditions = getPaymentConditionUseCase(billing.brand)
                            _state.update { it.copy(paymentConditionList = paymentConditions) }
                        } catch (e: Exception) {
                            Log.e("MdcAppOnly", "Error loading PaymentConditions: $e")
                        }
                    }
                }

                _state.update {
                    it.copy(
                        billing = billing.recalculate(),
                        isLoading = false
                    )
                }
                Log.i("MdcAppOnly", "DetailInvoiceViewModel --- reactive billing update: $billing")
            }.launchIn(viewModelScope)

        observePaymentsRegisterUseCase(invoiceNumber)
            .onEach { payments ->
                _state.update { it.copy(payments = payments) }
            }.launchIn(viewModelScope)
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

    @androidx.annotation.RequiresApi(26)
    fun registerPayment(amount: Double) {
        viewModelScope.launch {
            val current = _state.value

            // 1. Get Last ID
            val lastId = getLastIdPaymentUseCase()
            val nextId = lastId + 1

            // 2. Create Payment Register
            val now = try {
                val date = java.time.LocalDate.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                date.format(formatter)
            } catch (e: Exception) {
                ""
            }

            val paymentRegister = PaymentRegisterModel(
                id = nextId,
                clientId = current.billing.clientId,
                branch = current.billing.brand,
                date = now,
                clientName = current.billing.clientName,
                documentNumber = current.billing.billingNumber,
                type = current.billing.type.ifEmpty { "Factura" },
                total = amount
            )

            // 3. Save Payment
            val registerResult = addPaymentToRegisterUseCase(paymentRegister)

            if (registerResult) {
                // 4. Update Billing payed total
                val updatedBilling = current.billing.copy(
                    payed = current.billing.payed + amount
                ).recalculate()

                _state.update {
                    it.copy(
                        billing = updatedBilling,
                        payments = it.payments + paymentRegister,
                        message = "Pago registrado"
                    )
                }
                saveBilling()
            } else {
                _state.update { it.copy(message = "Error al registrar el pago") }
            }
        }
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
