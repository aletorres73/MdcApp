package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.service.UserService
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import com.mdcapp.domain.usescases.invoiceusecase.InvoiceUseCase
import com.mdcapp.domain.usescases.ordersusescases.BuyOrderUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DashboardViewModel(
    private val userService: UserService,
    private val getClientsUseCase: GetClientsUseCase,
    private val observeAllBillings: InvoiceUseCase.ObserveAllBillings,
    private val observeAllBuyOrders: BuyOrderUseCase.ObserveAllBuyOrders,
    private val analytics: AnalyticsService,
    private val refreshDatabaseUseCase: InvoiceUseCase.RefreshDatabase
) : ViewModel() {

    data class UiState(
        val userName: String = "",
        val monthlyOrdersCount: Int = 0,
        val monthlyBillingTotal: Double = 0.0,
        val activeClientsCount: Int = 0,
        val monthlyCollectedTotal: Double = 0.0,
        val monthlyPendingCollectedTotal: Double = 0.0,
        val currentMonthName: String = "",
        val allOrders: List<BuyOrderModel> = emptyList(),
        val filteredOrders: List<BuyOrderModel> = emptyList(),
        val allInvoices: List<BillingModel> = emptyList(),
        val filteredInvoices: List<BillingModel> = emptyList(),
        val ordersSearchQuery: String = "",
        val invoicesSearchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        analytics.logScreenView("DashboardHome")
        observeData()
    }

    private fun observeData() {
        _uiState.update { it.copy(isLoading = true) }

        // Observe User Profile
        viewModelScope.launch {
            userService.observeUserProfile()
                .catch { Napier.e("Error observing user profile", it) }
                .collect { profile ->
                    val firstName =
                        profile?.name?.trim()?.split(" ")?.firstOrNull { it.isNotBlank() } ?: ""
                    _uiState.update { it.copy(userName = firstName) }
                }
        }

        // Observe Clients
        viewModelScope.launch {
            getClientsUseCase.observeAll()
                .catch { Napier.e("Error observing clients", it) }
                .collect { clients ->
                    val activeCount = clients.count { it.isActive }
                    _uiState.update { it.copy(activeClientsCount = activeCount) }
                }
        }

        // Combine Invoices & Orders Flow
        combine(
            observeAllBillings(),
            observeAllBuyOrders()
        ) { billings, orders ->
            val now = LocalDate.now()
            val currentYear = now.year
            val currentMonth = now.month

            // Monthly stats
            val monthlyOrders = orders.filter { order ->
                val dateMillis = if (order.timeStamp > 0) order.timeStamp else order.loadedDate
                if (dateMillis <= 0) false else {
                    val dt = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    dt.year == currentYear && dt.month == currentMonth
                }
            }

            val monthlyInvoices = billings.filter { invoice ->
                val dateMillis = if (invoice.loadDate > 0) invoice.loadDate else invoice.timeStamp
                if (dateMillis <= 0) false else {
                    val dt = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    dt.year == currentYear && dt.month == currentMonth
                }
            }

            val billingTotal = monthlyInvoices.sumOf { it.total }
            val collectedTotal = monthlyInvoices.sumOf { it.payed }
            val pendingCollectedTotal = monthlyInvoices.sumOf { maxOf(0.0, it.rest) }

            val sortedOrders =
                orders.sortedByDescending { if (it.timeStamp > 0) it.timeStamp else it.loadedDate }
            val sortedInvoices =
                billings.sortedByDescending { if (it.timeStamp > 0) it.timeStamp else it.loadDate }

            _uiState.update { current ->
                current.copy(
                    monthlyOrdersCount = monthlyOrders.size,
                    monthlyBillingTotal = billingTotal,
                    monthlyCollectedTotal = collectedTotal,
                    monthlyPendingCollectedTotal = pendingCollectedTotal,
                    currentMonthName = formatMonthName(now),
                    allOrders = sortedOrders,
                    filteredOrders = filterOrders(sortedOrders, current.ordersSearchQuery),
                    allInvoices = sortedInvoices,
                    filteredInvoices = filterInvoices(sortedInvoices, current.invoicesSearchQuery),
                    isLoading = false
                )
            }
        }.catch { e ->
            Napier.e("Error observing dashboard data", e)
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }.launchIn(viewModelScope)
    }

    fun onOrdersQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                ordersSearchQuery = query,
                filteredOrders = filterOrders(state.allOrders, query)
            )
        }
    }

    fun onInvoicesQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                invoicesSearchQuery = query,
                filteredInvoices = filterInvoices(state.allInvoices, query)
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                getClientsUseCase.resetPagination()
                refreshDatabaseUseCase()
                kotlinx.coroutines.delay(800)
            } catch (e: Exception) {
                Napier.e("Error refreshing dashboard", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun filterOrders(list: List<BuyOrderModel>, query: String): List<BuyOrderModel> {
        val q = query.trim().lowercase()
        val source = if (q.isEmpty()) list.take(10) else list
        if (q.isEmpty()) return source
        return source.filter {
            it.order.lowercase().contains(q) ||
                    it.client.lowercase().contains(q) ||
                    it.factory.lowercase().contains(q) ||
                    it.branch.lowercase().contains(q)
        }
    }

    private fun filterInvoices(list: List<BillingModel>, query: String): List<BillingModel> {
        val q = query.trim().lowercase()
        val source = if (q.isEmpty()) list.take(10) else list
        if (q.isEmpty()) return source
        return source.filter {
            it.billingNumber.lowercase().contains(q) ||
                    it.clientName.lowercase().contains(q) ||
                    it.brand.lowercase().contains(q) ||
                    it.stateBilling.lowercase().contains(q)
        }
    }

    private fun formatMonthName(now: LocalDate): String {
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val monthName = months.getOrElse(now.monthValue - 1) { "" }
        return "$monthName ${now.year}".trim()
    }
}
