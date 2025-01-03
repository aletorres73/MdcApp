package com.mdcapp.ui.viewmodels.orders

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import kotlinx.coroutines.launch

class OrdersViewModel(private val getAllOrders: OrdersUseCase.GetAllOrders) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    data class UiState(
        val loading: Boolean = true,
        val orderList: List<OrderModel> = emptyList(),
        var filters: MutableMap<String, Boolean> = mutableMapOf(
            "Pending" to false,
            "Progress" to false,
            "Closed" to false,
        )
    )

    init {
        viewModelScope.launch {
            fetchFromRepository()
        }
    }

    private suspend fun fetchFromRepository() {
        val orderList = getAllOrders()
        state = state.copy(
            loading = false,
            orderList = orderList.sortedByDescending { it.orderNumber }
        )
    }

    fun filterListByOrderState(filter: String, value: Boolean) {
        viewModelScope.launch {
            state = state.copy(
                filters = state.filters.apply { this[filter] = value }
            )
            Log.i("Home", "OrdersViewModel: ${state.filters}")
        }
    }

}
