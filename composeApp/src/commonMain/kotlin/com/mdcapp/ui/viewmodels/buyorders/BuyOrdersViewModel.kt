package com.mdcapp.ui.viewmodels.buyorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.domain.usescases.homeusescases.PaymentConditionsUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.launch

class BuyOrdersViewModel(
    private val getBuyOrder: BuyOrderUseCase.GetBuyOrderById,
    private val getBillings: BuyOrderUseCase.GetBillings,
    private val getPaymentsConditions: PaymentConditionsUseCase.GetPaymentsConditions,
    private val addPaymentConditionsUseCase: PaymentConditionsUseCase.SetPaymentsConditionsFactory
) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set
    var tempState by mutableStateOf(state)
        private set

    data class UiState(
        val loadingOrder: Boolean = false,
        val loadingBillings: Boolean = false,
        val loadingPayments: Boolean = false,
        val orderId: String = "",
        val factoryName: String = "",
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val billings: List<BillingModel> = emptyList(),
        val totalAmount: Double = 0.0,
        val error: String? = null,
        val paymentsConditions: List<PaymentCondition> = emptyList(),
    )

    fun init(orderId: String, factoryName: String) {
        viewModelScope.launch {
            println("Init called with orderId: $orderId")
            state = state.copy(
                orderId = orderId,
                factoryName = factoryName
            )
            loadBuyOrder()
            loadBillings()
            loadPaymentConditions()
            tempState = state
        }
    }

    fun dataChanged() = state != tempState

    fun saveData() {
        state = tempState
        //agregar función para actualizar billing en la nube retornando boolean
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
                    billing.copy(
                        paymentCondition = paymentCondition.paymentName,
                        total = "$%.2f".format(total),
                        discount = "%.2f".format(discount).toDouble(),
                        toPay = "%.2f".format(toPay).toDouble()
                    )
                } else {
                    billing
                }
            }
        )
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
}