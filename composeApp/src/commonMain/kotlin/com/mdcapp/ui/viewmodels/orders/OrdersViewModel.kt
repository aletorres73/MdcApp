package com.mdcapp.ui.viewmodels.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.Order
import com.mdcapp.data.model.orderList
import com.mdcapp.domain.usescases.GetAllOrdersUseCase
import kotlinx.coroutines.launch

class OrdersViewModel(private val getOrdersUseCase: GetAllOrdersUseCase) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    private var data by mutableStateOf(Data())

    data class UiState(
        val loading: Boolean = false,
        val orderList: List<Order> = emptyList()
    )

    data class Data(
        val dataList: List<Order> = emptyList()
    )

    init {
        viewModelScope.launch {
            state = UiState(loading = false)
            fetchFromRepository()
        }
    }

    private suspend fun OrdersViewModel.fetchFromRepository() {
        data = Data(dataList = getOrdersUseCase())
        state = if (data.dataList.isNotEmpty()) {
            UiState(
                loading = false,
                orderList = data.dataList
            )
        } else {
            UiState(loading = true)
        }
        println("OrdersViewModel fetch from repository: ${data.dataList}")
    }

}