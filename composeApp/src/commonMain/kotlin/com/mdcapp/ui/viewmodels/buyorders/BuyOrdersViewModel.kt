package com.mdcapp.ui.viewmodels.buyorders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.usescases.OrdersUseCase
import kotlinx.coroutines.launch

class BuyOrdersViewModel(private val getAllOrders: OrdersUseCase.GetAllOrders) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    private var data by mutableStateOf(Data())

    data class UiState(
        val loading: Boolean = false,
        val orderList: List<OrderModel> = emptyList()
    )

    data class Data(
        val dataList: List<OrderModel> = emptyList()
    )

    init {
        viewModelScope.launch {
            state = UiState(loading = false)
            fetchFromRepository()
        }
    }

    private suspend fun BuyOrdersViewModel.fetchFromRepository() {
        data = Data(dataList = getAllOrders())
        state = if (data.dataList.isNotEmpty()) {
            UiState(
                loading = false,
                orderList = data.dataList.sortedByDescending { it.orderNumber }
            )
        } else {
            UiState(loading = true)
        }
    }

}