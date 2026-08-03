package com.mdcapp.ui.viewmodels.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ClientOrdersViewModel(
    clientId: String,
    private val observeBuyOrdersUseCase: BuyOrderUseCase.ObserveBuyOrdersByClient,
    private val analytics: AnalyticsService
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
        analytics.logScreenView("ClientOrders", clientId)
        loadOrders(clientId)
    }

    fun loadOrders(clientId: String) {
        _state.update { it.copy(isLoading = true) }
        try {
            observeBuyOrdersUseCase(clientId)
                .onEach { orders ->
                    _state.update { it.copy(isLoading = false, orders = orders) }
                }.launchIn(viewModelScope)
        } catch (e: Exception) {
            Napier.e("Error observing client orders", e)
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}

