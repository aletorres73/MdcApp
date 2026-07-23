package com.mdcapp.ui.viewmodels.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientOrdersViewModel(
    clientId: String,
    private val getBuyOrdersUseCase: BuyOrderUseCase.GetBuyOrdersByClient
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(clientId = clientId))
    val state = _state.asStateFlow()

    data class UiState(
        val clientId: String = "",
        val isLoading: Boolean = false,
        val orders: List<BuyOrderModel> = emptyList(),
        val error: String? = null
    )

    init {
        loadOrders(clientId)
    }

    fun loadOrders(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val orders = getBuyOrdersUseCase(clientId)
                _state.update { it.copy(isLoading = false, orders = orders) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
