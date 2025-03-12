package com.mdcapp.ui.viewmodels.orders

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.usescases.ordersusescases.GetFactoriesListUseCase
import com.mdcapp.domain.usescases.ordersusescases.OrdersUseCase
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val getOrdersByFactory: OrdersUseCase.GetOrdersByFactory,
    private val getBranchUseCase: OrdersUseCase.GetOrderBranch,
    private val getFactoriesList: GetFactoriesListUseCase
) : ViewModel() {
    var state by mutableStateOf(UiState())
        private set

    private val _branches = mutableStateMapOf<String, String>()
    val branch: Map<String, String> get() = _branches

    data class UiState(
        val loading: Boolean = false,
        val factoryName: String = "",
        val factoriesList: List<String> = emptyList(),
        val backupList: List<OrderModel> = emptyList(), // copia original de la lista del repo
        val orderList: List<OrderModel> = emptyList(), // lista para mostrar en pantalla
        val filteredOrderList: List<OrderModel> = emptyList(),
        var filters: MutableMap<String, Boolean> = mutableMapOf(
            "Pending" to false,
            "Progress" to false,
            "Closed" to false,
        ),
        val filterFactory: MutableMap<String, Boolean> = mutableMapOf(),
        var query: TextFieldValue = TextFieldValue(),
        val isSearchBar: Boolean = false,
//        val branch: String = ""
    )

    fun init(factoryName: String) {
        viewModelScope.launch {
            state = state.copy(factoryName = factoryName)
            fetchFromRepository()
            if (state.query.text.isNotEmpty()) {
                searchOrders(state.query)
                setSearchBar(true)
            }
        }
    }

    fun setSearchBar(value: Boolean) {
        viewModelScope.launch {
            state = state.copy(isSearchBar = value)
        }
    }

    fun getBranchOrder(orderId: String) {
        viewModelScope.launch {
            val branch = getBranchUseCase(orderId)
            _branches[orderId] = branch
        }
    }

    suspend fun fetchFromRepository() {
        state = state.copy(loading = true)
        val remoteList =
            getOrdersByFactory(state.factoryName).sortedByDescending { it.orderNumber }
        val factoriesList = getFactoriesList()
        if (factoriesList.isNotEmpty()) factoriesList.forEach { factory ->
            state.filterFactory[factory] = false
        }
        state = state.copy(
            loading = false,
            orderList = remoteList,
            backupList = remoteList,
            factoriesList = factoriesList
        )
    }

    private fun applyFilters(
        list: List<OrderModel>,
        filters: Map<String, Boolean> = state.filters,
        flag: String,
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

    private fun applyFilterFactory(
        list: List<OrderModel>,
        filters: Map<String, Boolean> = state.filters,
        flag: String,
    ): List<OrderModel> {
        println(filters)
        if (state.factoriesList.isEmpty() && flag == "filtered") return list
        if (state.factoriesList.isEmpty() && flag == "no_filtered") return emptyList()
        return when (flag) {
            "filtered" -> {
                list.filter { order ->
                    !filters.any { (factory, isSelected) ->
                        if (factory == "IBA") {
                            !(isSelected && order.branch == "Gummi" || isSelected && order.branch == "Kids" || isSelected && order.branch == "Diamond")
                        } else
                            isSelected && order.branch != factory
                    }
                }
            }

            "no_filtered" -> {
                list.filter { order ->
                    filters.any { (factory, isSelected) ->
                        if (factory == "IBA")
                            (isSelected && order.branch == "Gummi" || isSelected && order.branch == "Kids" || isSelected && order.branch == "Diamond")
                        else
                            isSelected && order.branch == factory
                    }
                }
            }

            else -> {
                list
            }
        }

    }

    fun filterOrdersByFactory(factory: String, pressed: Boolean) {
        viewModelScope.launch {
            updateFilters(factory, pressed, "factory")
            if (state.query.text.isEmpty()) {
                state = if (isAnyFilterFactoryActive()) {
                    val newList =
                        applyFilterFactory(
                            state.backupList,
                            state.filterFactory,
                            flag = "no_filtered"
                        )
                    val filteredList =
                        applyFilterFactory(state.backupList, state.filterFactory, flag = "filtered")
                    state.copy(
                        loading = false,
                        orderList = newList,
                        filteredOrderList = filteredList
                    )
                } else {
                    state.copy(
                        loading = false,
                        orderList = state.backupList,
                        filteredOrderList = emptyList()
                    )
                }
            } else {
                searchOrders(state.query, true)
            }
            Log.i("Home", "OrdersViewModel: ${state.filters}")
        }
    }

    fun filterListByOrderState(filter: String, value: Boolean) {
        viewModelScope.launch {
            updateFilters(filter, value)

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
                searchOrders(state.query, true)
            }
            Log.i("Home", "OrdersViewModel: ${state.filters}")
        }
    }

    private fun updateFilters(keyFilter: String, value: Boolean, flag: String = "") {
        state = state.copy(loading = true)
        if (flag != "factory") {
            val updatedFilters = state.filters.mapValues { (key, _) ->
                key == keyFilter && value
            }.toMutableMap()
            state = state.copy(filters = updatedFilters)
        } else {
            val updatedFilters = state.filterFactory.mapValues { (key, _) ->
                key == keyFilter && value
            }.toMutableMap()
            state = state.copy(filterFactory = updatedFilters)
        }
    }

    fun searchOrders(query: TextFieldValue, factoryFilter: Boolean = false) {
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
                    if (isAnyFilterFactoryActive())
                        searchInList(
                            if (!factoryFilter)
                                applyFilters(
                                    state.backupList,
                                    flag = "no_filtered"
                                )
                            else
                                applyFilterFactory(
                                    state.backupList,
                                    state.filterFactory,
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

    private fun isAnyFilterFactoryActive() = state.filterFactory.any { it.value }

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
