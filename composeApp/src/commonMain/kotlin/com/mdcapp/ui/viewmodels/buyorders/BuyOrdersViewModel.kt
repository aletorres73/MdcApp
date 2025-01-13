package com.mdcapp.ui.viewmodels.buyorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.launch

class BuyOrdersViewModel(
    private val getBuyOrder: BuyOrderUseCase.GetBuyOrderById,
    private val getBillings: BuyOrderUseCase.GetBillings,
) : ViewModel() {

    var state by mutableStateOf(UiState())
        private set

    data class UiState(
        val loadingOrder: Boolean = false,
        val loadingBillings: Boolean = false,
        val orderId: String = "",
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val billings: List<BillingModel> = emptyList(),
        val totalAmount: Double = 0.0,
        val error: String? = null
    )

    fun init(orderId: String) {
        println("Init called with orderId: $orderId")
        state = state.copy(orderId = orderId)
        loadBuyOrder()
        loadBillings()
    }

    private fun loadBuyOrder() {
        viewModelScope.launch {
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
    }

    private fun loadBillings() {
        viewModelScope.launch {
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