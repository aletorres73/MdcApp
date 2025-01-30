package com.mdcapp.ui.viewmodels.orders

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val getOrdersByFactory: OrdersUseCase.GetOrdersByFactory,
) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    data class UiState(
        val loading: Boolean = false,
        val factoryName: String = "",
        val backupList: List<OrderModel> = emptyList(), // copia original de la lista del repo
        val orderList: List<OrderModel> = emptyList(), // lista para mostrar en pantalla
        val filteredOrderList: List<OrderModel> = emptyList(),
        var filters: MutableMap<String, Boolean> = mutableMapOf(
            "Pending" to false,
            "Progress" to false,
            "Closed" to false,
        ),
        var query: TextFieldValue = TextFieldValue()
    )

    fun init(factoryName: String) {
        viewModelScope.launch {
            state = state.copy(factoryName = factoryName)
            fetchFromRepository()
        }
    }

    suspend fun fetchFromRepository() {
        state = state.copy(loading = true)
        val remoteList =
            getOrdersByFactory(state.factoryName).sortedByDescending { it.orderNumber }
        state = state.copy(
            loading = false,
            orderList = remoteList,
            backupList = remoteList
        )
    }

    private fun applyFilters(
        list: List<OrderModel>,
        filters: Map<String, Boolean> = state.filters,
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
            val updatedFilters = state.filters.mapValues { (key, filterValue) ->
                key == filter && value
            }.toMutableMap()
            state = state.copy(filters = updatedFilters)

            if (state.query.text.isEmpty()) {
                state = if (isAnyFilterActive()) {
                    val newList = applyFilters(state.backupList, flag = "no_filtered")
                    val filteredList = applyFilters(state.backupList, flag = "filtered")
                    state.copy(
                        loading = false,
                        orderList = newList,
                        filteredOrderList = filteredList
                    )
                } else
                    state.copy(
                        loading = false,
                        orderList = state.backupList,
                        filteredOrderList = emptyList()
                    )
            } else {
                searchOrders(state.query)
            }
            Log.i("Home", "OrdersViewModel: ${state.filters}")
        }
    }

    fun searchOrders(query: TextFieldValue) {
        fun searchInList(list: List<OrderModel>, searchText: String): List<List<OrderModel>> {
            val matchedOrders = list.filter { order ->
                order.orderNumber.contains(searchText, ignoreCase = true) ||
                        order.nameClient.contains(searchText, ignoreCase = true)
            }
            val unmatchedOrders = list.filter { order ->
                !order.orderNumber.contains(searchText, ignoreCase = true) &&
                        !order.nameClient.contains(searchText, ignoreCase = true)
            }
            return listOf(matchedOrders, unmatchedOrders)
        }
        viewModelScope.launch {
            val searchText = query.text.trim().lowercase()
            state = if (searchText.isNotEmpty()) {
                val filteredBySearch =
                    if (isAnyFilterActive())
                        searchInList(
                            applyFilters(
                                state.backupList,
                                flag = "no_filtered"
                            ),
                            searchText
                        )
                    else
                        searchInList(state.backupList, searchText)
                state.copy(
                    loading = false,
                    filteredOrderList = (filteredBySearch[1] + state.filteredOrderList).distinct(),
                    orderList = filteredBySearch[0],
                    query = query
                )
            } else
                state.copy(
                    loading = false,
                    filteredOrderList = emptyList(),
                    orderList = state.backupList,
                    query = query
                )
        }
    }

    private fun isAnyFilterActive() = state.filters.any { it.value }

    fun cleanSearchQuery() {
        fun reset() {
            val resetFilters = state.filters.mapValues { false } as MutableMap
            state = state.copy(
                loading = false,
                filteredOrderList = emptyList(),
                orderList = state.backupList,
                query = TextFieldValue(),
                filters = resetFilters
            )
        }
        viewModelScope.launch {
            reset()
        }
    }
}
