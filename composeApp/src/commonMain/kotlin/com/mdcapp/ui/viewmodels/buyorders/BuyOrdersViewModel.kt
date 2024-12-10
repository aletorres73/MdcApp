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
    private val getBillings: BuyOrderUseCase.GetBillings
) : ViewModel() {
    var stateBuyOrder by mutableStateOf(UiStateBuyOrder())
        private set
    var stateBillings by mutableStateOf(UiStateBillings())
        private set

    private var buyOrderId by mutableStateOf(String())
    private var orderId by mutableStateOf(String())

    data class UiStateBuyOrder(
        val loading: Boolean = false,
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val error: String? = null
    )

    data class UiStateBillings(
        val loading: Boolean = false,
        val billings: List<BillingModel> = emptyList(),
        val error: String? = null
    )

    /*    init {
            loadBuyOrder()
            loadBillings()
        }*/

    private fun loadBuyOrder() {
        viewModelScope.launch {
            stateBuyOrder = stateBuyOrder.copy(loading = true)
            try {
                if (buyOrderId.isNotEmpty()) {
                    stateBuyOrder = stateBuyOrder.copy(
                        loading = false,
                        buyOrder = getBuyOrder(buyOrderId)
                    )
                }
            } catch (e: Exception) {
                stateBuyOrder = stateBuyOrder.copy(
                    loading = false,
                    error = e.message
                )
            }

        }
    }

    private fun loadBillings() {
        viewModelScope.launch {
            stateBillings = stateBillings.copy(loading = true)
            try {
                if (buyOrderId.isNotEmpty()) {
                    stateBillings = stateBillings.copy(
                        loading = false,
                        billings = getBillings(orderId)
                    )
                }
            } catch (e: Exception) {
                stateBillings = stateBillings.copy(
                    loading = false,
                    error = e.message
                )
            }

        }
    }

    fun fetchBuyOrder(id: String) {
        buyOrderId = id
        loadBuyOrder()
    }

    fun fetchBillings(id: String) {
        orderId = id
        loadBillings()
    }

}