package com.mdcapp.ui.viewmodels.buyorders

import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.entities.recalculate
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(26)
class BuyOrdersViewModel(
    private val getBuyOrder: BuyOrderUseCase.GetBuyOrderById,
    private val getBillings: BuyOrderUseCase.GetBillings,
    private val getPaymentsConditions: PaymentConditionsUseCase.GetPaymentsConditions,
    private val addPaymentToRegister: BuyOrderUseCase.AddPaymentToRegister,
    private val getLastId: BuyOrderUseCase.GetLastIdPaymentFromRegister,
    private val updateBilling: BuyOrderUseCase.UpdateBilling,
    private val getPaymentsRegister: BuyOrderUseCase.GetPaymentsRegister
) : ViewModel() {

    // Estado inmutable expuesto a la UI
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // Estado temporal para ediciones
    private val _tempState = MutableStateFlow(_state.value)
    val tempState: StateFlow<UiState> = _tempState.asStateFlow()

    // Definición del estado de la UI
    data class UiState(
        val loadingOrder: Boolean = false,
        val loadingBillings: Boolean = false,
        val loadingPayments: Boolean = false,
        val loadingPaymentsRegister: Boolean = false,
        val clientId: String = "",
        val orderId: String = "",
        val factoryName: String = "",
        val buyOrder: BuyOrderModel = BuyOrderModel(),
// ...
        val billings: List<BillingModel> = emptyList(),
        val totalAmount: Double = 0.0,
        val totalToPay: Double = 0.0,
        val totalDiscount: Double = 0.0,
        val totalPayed: Double = 0.0,
        val totalRest: Double = 0.0,
        val error: String? = null,
        val paymentsConditions: List<PaymentCondition> = emptyList(),
        val result: Boolean = true,
        val paymentsRegisterNotEmpty: Boolean = false,
        val paymentList: List<PaymentRegisterModel> = emptyList()
    )

    // Inicialización del ViewModel
    fun init(clientId: String, orderId: String, factoryName: String) {
        viewModelScope.launch {
            _state.value =
                _state.value.copy(clientId = clientId, orderId = orderId, factoryName = factoryName)

            // Cargar datos en paralelo
            val paymentConditionsDeferred = async { loadPaymentConditions() }
            val buyOrderDeferred = async { loadBuyOrder(clientId) }
            val billingsDeferred = async { loadBillings(clientId) }
//            val paymentsRegisterDeferred = async {  loadPaymentsRegister() }

            paymentConditionsDeferred.await()
            buyOrderDeferred.await()
            billingsDeferred.await()
            _tempState.value = _state.value
        }
    }

    fun dataChanged(): Boolean {
        return _state.value != _tempState.value
    }

    // Cargar condiciones de pago
    private suspend fun loadPaymentConditions() {
        _state.value = _state.value.copy(loadingPayments = true)
        try {
            val paymentsConditions = getPaymentsConditions(factoryName = _state.value.factoryName)
            _state.value = _state.value.copy(
                paymentsConditions = paymentsConditions,
                loadingPayments = false
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // Cargar la orden de compra
    private suspend fun loadBuyOrder(clientId: String) {
        _state.value = _state.value.copy(loadingOrder = true)
        try {
            if (_state.value.orderId.isNotEmpty()) {
                val buyOrder = getBuyOrder(clientId, _state.value.orderId)
                _state.value = _state.value.copy(
                    loadingOrder = false,
                    buyOrder = buyOrder
                )
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // Cargar facturas
    private suspend fun loadBillings(clientId: String) {
        _state.value = _state.value.copy(loadingBillings = true)
        try {
            if (_state.value.orderId.isNotEmpty()) {
                val billings = getBillings(clientId, _state.value.orderId)
                val totalAmount = calculateTotalBillingAmount(billings)
                _state.value = _state.value.copy(
                    loadingBillings = false,
                    billings = billings,
                    totalAmount = totalAmount
                )
                loadPaymentsRegister()
                loadTotalsPayments()
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // Cargar registros de pagos
    private suspend fun loadPaymentsRegister() {
        _state.value = _state.value.copy(loadingPaymentsRegister = true)
        try {
            if (_state.value.orderId.isNotEmpty()) {
                val documentsList = _state.value.billings.map { it.billingNumber }
                val payments = getPaymentsRegister(documentsList)
                _state.value = if (payments.isNotEmpty()) {
                    _state.value.copy(
                        loadingPaymentsRegister = false,
                        paymentsRegisterNotEmpty = true,
                        paymentList = payments.sortedByDescending { it.date }
                    )
                } else {
                    _state.value.copy(
                        loadingPaymentsRegister = false,
                        paymentsRegisterNotEmpty = false
                    )
                }
                checkPaymentsOnBillings()
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // Verificar pagos en facturas
    private fun checkPaymentsOnBillings() {
        /* val paymentsByBilling = _state.value.paymentList.groupBy { it.documentNumber }
         _state.value = _state.value.copy(
             billings = _state.value.billings.map { billing ->
                 val payments = paymentsByBilling[billing.billingNumber] ?: emptyList()
                 val totalPayed = payments.sumOf { it.total }
                 billing.copy(
                     payed = "%.2f".format(Locale.US, totalPayed).toDouble(),
                     rest = "%.2f".format(Locale.US, billing.toPay - totalPayed).toDouble()
                 )
             }
         )
         loadTotalsPayments()*/
    }

    // Calcular totales de pagos
    private fun loadTotalsPayments() {
        /*        val totals =
                    _state.value.billings.fold(Triple(0.0, 0.0, 0.0)) { (toPay, payed, rest), billing ->
                        Triple(toPay + billing.toPay, payed + billing.payed, rest + billing.rest)
                    }
                _state.value = _state.value.copy(
                    totalToPay = totals.first,
                    totalPayed = totals.second,
                    totalRest = totals.third
                )*/
    }

    // Manejo de errores centralizado
    private fun handleError(e: Exception) {
        _state.value = _state.value.copy(error = e.message)
        Log.e("BuyOrdersViewModel", "Error: ${e.message}")
    }

    // Guardar datos editados
    fun saveData(): Boolean {
        var result = false
        viewModelScope.launch {
            val clientId = _state.value.clientId
            val orderId = _state.value.orderId
            result = _tempState.value.billings.all { billing ->
                updateBilling(clientId, orderId, billing.billingNumber, billing)
            }
            if (result) {
                _state.value = _tempState.value
            } else {
                handleError(Exception("Error updating some billings"))
            }
        }
        return result
    }

    // Seleccionar condición de pago
    @RequiresApi(26)
    fun onSelectedPaymentCondition(paymentCondition: PaymentCondition, billingNumber: String) {
        _tempState.value = _tempState.value.copy(
            billings = _tempState.value.billings.map { billing ->
                if (billing.billingNumber == billingNumber) {
                    billing.copy(
                        paymentCondition = paymentCondition.paymentName,
                        discount = paymentCondition.discount
                    ).recalculate(paymentCondition)
                } else {
                    billing
                }
            }
        )
    }

    // Obtener fecha de pago
    private fun getPayDate(
        billingNumber: String,
        expiration: Int,
        newDeliveryDate: String = ""
    ): String {
        return "" // Simplified or unused now
    }

    // Formatear fecha
    private fun formatDateString(dateStr: String): String {
        val parts = dateStr.split("/")
        if (parts.size == 3 && parts[0].length == 2 && parts[1].length == 2 && parts[2].length == 4) {
            return dateStr
        }
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return "$day/$month/$year"
    }

    // Obtener fecha actual
    @RequiresApi(26)
    private fun getCurrentDate(): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.now().format(dateFormatter)
    }

    fun saveDateSelected(newDate: String, billingNumber: String) {
        try {
            val formatDate = formatDateString(newDate)
            _tempState.value = _tempState.value.copy(
                billings = _tempState.value.billings.map { billing ->
                    if (billing.billingNumber == billingNumber) {
                        val condition =
                            _tempState.value.paymentsConditions.find { it.paymentName == billing.paymentCondition }
                        billing.copy(deliveryDate = formatDate).recalculate(condition)
                    } else {
                        billing
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("BuyOrdersViewModel", "saveDateSelected: $e")
            _tempState.value =
                _tempState.value.copy(error = "Error al guardar la fecha: ${e.message}")
        }
    }

    @RequiresApi(26)
    fun addPayment(billingNumber: String, payed: Double) {
        viewModelScope.launch {
            try {
                val billing = _tempState.value.billings.find { it.billingNumber == billingNumber }
                val paymentToRegister = PaymentRegisterModel(
                    id = getLastId() + 1,
                    branch = _tempState.value.factoryName,
                    date = getCurrentDate(),
                    clientName = _tempState.value.buyOrder.client,
                    documentNumber = billingNumber,
                    type = billing?.type ?: "",
                    total = payed,
                    clientId = _tempState.value.buyOrder.clientId
                )

                if (addPaymentToRegister(paymentToRegister)) {
                    updateBillingPayment(billingNumber, payed)
                } else {
                    _tempState.value = _tempState.value.copy(error = "Error al registrar el pago")
                }
            } catch (e: Exception) {
                Log.e("BuyOrdersViewModel", "addPayment: $e")
                _tempState.value =
                    _tempState.value.copy(error = "Error al agregar el pago: ${e.message}")
            }
        }
    }

    private fun updateBillingPayment(billingNumber: String, payed: Double) {
        _tempState.value = _tempState.value.copy(
            billings = _tempState.value.billings.map { billing ->
                if (billing.billingNumber == billingNumber) {
                    billing.copy(
                        payed = billing.payed + payed
                    )
                } else {
                    billing
                }
            }
        )
        setRest(billingNumber)
    }

    private fun setRest(billingNumber: String) {
        /*        _tempState.value = _tempState.value.copy(
                    billings = _tempState.value.billings.map { billing ->
                        if (billing.billingNumber == billingNumber) {
                            billing.copy(
                                rest = "%.2f".format(Locale.US, billing.toPay - billing.payed).toDouble()
                            )
                        } else {
                            billing
                        }
                    }
                )*/
    }

    // Calcular el monto total de las facturas
    private fun calculateTotalBillingAmount(billings: List<BillingModel>): Double {
        return billings.sumOf {
            it.total.toString().replace("$", "").replace(",", "").toDouble()
        }
    }
}