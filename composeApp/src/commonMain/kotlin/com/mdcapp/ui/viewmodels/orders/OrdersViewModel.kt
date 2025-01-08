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
        val filteredOrderList: List<OrderModel> = emptyList(),
        var filters: MutableMap<String, Boolean> = mutableMapOf(
            "Pending" to false,
            "Progress" to false,
            "Closed" to false,
        ),
    )

    init {
        viewModelScope.launch { fetchFromRepository() }
    }

    suspend fun fetchFromRepository() {
        state = state.copy(loading = true)
        val orderList = getAllOrders()
        state = state.copy(
            loading = false,
            orderList = orderList.sortedByDescending { it.orderNumber }
        )
    }

    private fun applyFilters(
        list: List<OrderModel>,
        filters: Map<String, Boolean>,
        flag: String
    ): List<OrderModel> {
        return when (flag) {
            "filtered" -> {
                list.filter { order ->
                    !(filters["Pending"] == true && order.payState == "Sin información") &&
                            !(filters["Progress"] == true && (
                                    order.payState == "A cobrar" || order.payState == "Vencido" ||
                                            order.payState == "Por vencer" || order.payState == "A devolución"
                                    )) &&
                            !(filters["Closed"] == true && (
                                    order.payState == "Cobrado" || order.payState == "Cerrado" ||
                                            order.payState == "Devuelta" || order.payState == "Cancelado"
                                    ))
                }
            }

            "no_filtered" -> {
                list.filter { order ->
                    (filters["Pending"] == true && order.payState == "Sin información") ||
                            (filters["Progress"] == true && (
                                    order.payState == "A cobrar" || order.payState == "Vencido" ||
                                            order.payState == "Por vencer" || order.payState == "A devolución"
                                    )) ||
                            (filters["Closed"] == true && (
                                    order.payState == "Cobrado" || order.payState == "Cerrado" ||
                                            order.payState == "Devuelta" || order.payState == "Cancelado"
                                    ))
                }
            }

            else -> {
                list
            }
        }
    }

    fun filterListByOrderState(filter: String, value: Boolean) {
        viewModelScope.launch {
            state = state.copy(loading = true)
            val updatedFilters = state.filters.toMutableMap().apply {
                this[filter] = value
            }
            state = if (value) {
                val originalList = (state.orderList + state.filteredOrderList).distinct()
                state.copy(
                    loading = false,
                    filteredOrderList = applyFilters(originalList, updatedFilters, "filtered"),
                    orderList = applyFilters(originalList, updatedFilters, "no_filtered")
                )
            } else {
                state.copy(
                    loading = false,
                    orderList = (state.orderList + state.filteredOrderList)
                        .distinct()
                        .sortedByDescending { it.orderNumber }
                )
            }
            Log.i("Home", "OrdersViewModel: ${state.filters}")
        }
    }
}
