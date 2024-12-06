package com.mdcapp.ui.viewmodels.buyorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.domain.usescases.handlerusescases.HandlersUsesCases
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.launch

class BuyOrdersViewModel(
    private val getBuyOrder: BuyOrderUseCase.GetBuyOrderById,
    private val handlers: HandlersUsesCases
) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    private var buyOrderId by mutableStateOf(String())

    data class UiState(
        val loading: Boolean = false,
        val buyOrder: BuyOrderModel = BuyOrderModel(),
        val error: String? = null
    )

    init {
        loadBuyOrder()
    }

    private fun loadBuyOrder() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            try {
                if (buyOrderId.isNotEmpty()) {
                    state = state.copy(
                        loading = false,
                        buyOrder = getBuyOrder(buyOrderId)
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = e.message
                )
            }

        }
    }

    fun loadHandler(key: String, value: String): Boolean {
        buyOrderId = value
        loadBuyOrder()
        return if (handlers.loadValues(key, value)) {
            loadBuyOrder()
            true
        } else
            false
    }

}