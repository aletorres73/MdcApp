package com.mdcapp.ui.viewmodels.buyorders

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.data.model.PaymentRegisterModel
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BuyOrdersViewModel(
    private val getBuyOrder: BuyOrderUseCase.GetBuyOrderById,
    private val getBillings: BuyOrderUseCase.GetBillings,
    private val getPaymentsConditions: PaymentConditionsUseCase.GetPaymentsConditions,
    private val addPaymentToRegister: BuyOrderUseCase.AddPaymentToRegister,
    private val getLastId: BuyOrderUseCase.GetLastIdPaymentFromRegister,
    private val updateBilling: BuyOrderUseCase.UpdateBilling,
    private val getPaymentsRegister: BuyOrderUseCase.GetPaymentsRegister
//    private val addPaymentConditionsUseCase: PaymentConditionsUseCase.SetPaymentsConditionsFactory
) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set
    var tempState by mutableStateOf(state)
        private set

    data class UiState(
        val loadingOrder: Boolean = false,
        val loadingBillings: Boolean = false,
        val loadingPayments: Boolean = false,
        val loadingPaymentsRegister: Boolean = false,
        val orderId: String = "",
        val factoryName: String = "",
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val billings: List<BillingModel> = emptyList(),
        val totalAmount: Double = 0.0,
        var totalToyPay: Double = 0.0,
        var totalDiscount: Double = 0.0,
        var totalPayed: Double = 0.0,
        var totalRest: Double = 0.0,
        val error: String? = null,
        val paymentsConditions: List<PaymentCondition> = emptyList(),
        var result: Boolean = true,
        var paymentsRegisterNotEmpty: Boolean = false,
        var paymentList: List<PaymentRegisterModel> = emptyList()
    )

    fun init(orderId: String, factoryName: String) {
        viewModelScope.launch {
            println("Init called with orderId: $orderId")
            state = state.copy(
                orderId = orderId,
                factoryName = factoryName
            )
            loadPaymentConditions()
            loadBuyOrder()
            loadBillings()
            loadPaymentsRegister()
            tempState = state
        }
    }

    private suspend fun loadPaymentsRegister() {
        state = state.copy(loadingPaymentsRegister = true)
        try {
            if (state.orderId.isNotEmpty()) {
                val documentsList = state.billings.map { it.billingNumber }
                val payments = getPaymentsRegister(documentsList)
                println("Payments loaded: $payments")
                state = if (payments.isNotEmpty()) state.copy(
                    loadingPaymentsRegister = false,
                    paymentsRegisterNotEmpty = true,
                    paymentList = payments
                )
                else state.copy(
                    loadingPaymentsRegister = false,
                    paymentsRegisterNotEmpty = false
                )
                checkPaymentsOnBillings()
            }
        } catch (e: Exception) {
            Log.e("LoadPaymentsRegister", "Error loading payments: ${e.message}")
            state = state.copy(
                loadingPaymentsRegister = false,
                error = e.message
            )
        }
    }

    private fun checkPaymentsOnBillings() {
        state.billings.forEach { billing ->
            var payed = billing.payed
            if (payed == 0.0) {
                val payedRegistered =
                    state.paymentList.filter { it.documentNumber == billing.billingNumber }
                if (payedRegistered.isNotEmpty()) {
                    payedRegistered.forEach { payed += it.total }
                    println("Total on ${billing.billingNumber}: $payedRegistered")
                    println("Total sum: $payed")
                    state = state.copy(
                        billings = state.billings.map {
                            if (it.billingNumber == billing.billingNumber) {
                                val toPay = it.toPay
                                it.copy(
                                    payed = "%.2f".format(payed).toDouble(),
                                    rest = "%.2f".format(toPay - payed).toDouble()
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
            }
        }
//        loadTotalsPayments()
    }

    private fun loadTotalsPayments() {
        var totalToyPay = 0.0
        var totalDiscount = 0.0
        var totalPayed = 0.0
        var totalRest = 0.0

        state.billings.forEach {
            totalToyPay += it.toPay
            totalDiscount += it.discount
            totalPayed += it.payed
            totalRest += it.rest
        }
        state = state.copy(
            totalDiscount = totalDiscount,
            totalToyPay = totalToyPay,
            totalPayed = totalPayed,
            totalRest = totalRest
        )
    }

    fun dataChanged() = state != tempState

    fun saveData() {
        viewModelScope.launch {
            var result = true // Inicializar result en true
            tempState.billings.forEach {
                result =
                    result && updateBilling(it.billingNumber, it) // Acumular el resultado booleano
                if (result) {
                    println("Billing ${it.billingNumber} updated")
                } else {
                    println("Error on update billing ${it.billingNumber}")
                }
            }
            if (result) {
                state = tempState
                println("All billings updated successfully")
            } else {
                println("Error updating some billings")
            }
        }
    }


    fun onSelectedPaymentCondition(paymentCondition: PaymentCondition, billingNumber: String) {
        tempState = tempState.copy(
            billings = tempState.billings.map { billing ->
                if (billing.billingNumber == billingNumber) {
                    val total =
                        billing.total
                            .replace("$", "")
                            .replace(",", "")
                            .toDoubleOrNull() ?: 0.0
                    val discount = paymentCondition.discount * total
                    val toPay = total * (1.0 - paymentCondition.discount)
                    val payDate = getPayDate(
                        billingNumber,
                        paymentCondition.expiration
                    )
                    billing.copy(
                        paymentCondition = paymentCondition.paymentName,
                        total = "$%.2f".format(Locale.US, total),
                        discount = "%.2f".format(Locale.US, discount).toDouble(),
                        toPay = "%.2f".format(Locale.US, toPay).toDouble(),
                        payDate = payDate
                    )
                } else {
                    billing
                }
            }
        )
    }

    fun saveDateSelected(newDate: String, billingNumber: String) {
        val formatDate = formatDateString(newDate)
        val paymentConditionName =
            tempState.billings.find { it.billingNumber == billingNumber }?.paymentCondition
        val expiration = paymentConditionName
            ?.let { name -> tempState.paymentsConditions.find { it.paymentName == name }?.expiration }
        try {
            tempState = tempState.copy(
                billings = tempState.billings.map { billing ->
                    if (billing.billingNumber == billingNumber) {
                        billing.copy(
                            deliveryDate = formatDate,
                            payDate = expiration?.let { getPayDate(billingNumber, it, formatDate) }
                                ?: ""
                        )
                    } else
                        billing
                }
            )
        } catch (e: Exception) {
            Log.e("BuyOrdersViewModel", "saveDateSelected : $e")
        }
    }

    private fun getPayDate(
        billingNumber: String,
        expiration: Int,
        newDeliveryDate: String = ""
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val billing = tempState.billings.find { it.billingNumber == billingNumber }
        val newPayDate = billing?.let {
            try {
                val deliveryDateStr = formatDateString(
                    newDeliveryDate.ifEmpty { it.deliveryDate.ifEmpty { newDeliveryDate } }
                )
                val deliveryDate = LocalDate.parse(deliveryDateStr, dateFormatter)
                val newDate = deliveryDate.plusDays(expiration.toLong())
                newDate.format(dateFormatter)
            } catch (e: Exception) {
                Log.e("BuyOrdersViewModel", "getPayDate: $e")
                ""
            }
        }
        return newPayDate ?: ""
    }

    private fun formatDateString(dateStr: String): String {
        val parts = dateStr.split("/")
        if (parts.size == 3 &&
            parts[0].length == 2 &&
            parts[1].length == 2 &&
            parts[2].length == 4
        ) {
            return dateStr // La fecha ya está en el formato correcto
        }
        // Formatear fecha si no está en el formato correcto
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return "$day/$month/$year"
    }

    private fun getCurrentDate(): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDate = LocalDate.now()
        return currentDate.format(dateFormatter)
    }

    private suspend fun loadPaymentConditions() {
        state = state.copy(loadingPayments = true)
        state = try {
            state.copy(
                paymentsConditions = getPaymentsConditions(factoryName = state.factoryName),
                loadingPayments = false
            )
        } catch (e: Exception) {
            state.copy(
                loadingPayments = false,
                error = e.message
            )
        }
        println("${state.paymentsConditions}")
    }

    private suspend fun loadBuyOrder() {
        state = state.copy(loadingOrder = true)
        try {
            if (state.orderId.isNotEmpty()) {
                val buyOrder = getBuyOrder(state.orderId)
                println("BuyOrder loaded: $buyOrder")
                state = state.copy(
                    loadingOrder = false,
                    buyOrder = buyOrder,
                )
            }
        } catch (e: Exception) {
            state = state.copy(
                loadingOrder = false,
                error = e.message
            )
        }
    }

    private suspend fun loadBillings() {
        state = state.copy(loadingBillings = true)
        try {
            if (state.orderId.isNotEmpty()) {
                val billings = getBillings(state.orderId)
                val totalAmount = calculateTotalBillingAmount(billings)
                println("Billings loaded: $billings")
                println("Total amount calculated: $totalAmount")
                state = state.copy(
                    loadingBillings = false,
                    billings = billings,
                    totalAmount = totalAmount
                )
                loadTotalsPayments()
            }
        } catch (e: Exception) {
            state = state.copy(
                loadingBillings = false,
                error = e.message
            )
        }
    }

    private fun calculateTotalBillingAmount(billings: List<BillingModel>): Double {
        var total = 0.0
        billings.forEach {
            val amount = it.total
                .replace("$", "")
                .replace(",", "")
                .toDouble()
            total += amount
        }
        return total
    }

    suspend fun addPayment(billingNumber: String, payed: Double) {
        viewModelScope.launch {
            val paymentToRegister = PaymentRegisterModel(
                id = getLastId() + 1,
                branch = tempState.factoryName,
                date = getCurrentDate(),
                clientName = tempState.buyOrder.client,
                documentNumber = billingNumber,
                type = tempState.billings.find { it.billingNumber == billingNumber }?.type ?: "",
                total = payed,
                clientId = tempState.buyOrder.clientId
            )
            if (addPaymentToRegister(paymentToRegister)) {
                tempState = tempState.copy(
                    billings = tempState.billings.map { billing ->
                        if (billing.billingNumber == billingNumber) {
                            billing.copy(
                                payed = billing.payed + payed,
                            )
                        } else billing
                    }
                )
                setRest(billingNumber)
            } else
                tempState = tempState.copy(error = " fail addPaymentToRegister")
        }
    }

    private fun setRest(billingNumber: String) {
        tempState = tempState.copy(
            billings = tempState.billings.map { billing ->
                if (billing.billingNumber == billingNumber) {
                    billing.copy(
                        rest = "%.2f".format(Locale.US, billing.toPay - billing.payed).toDouble()
                    )
                } else billing
            }
        )
    }
}