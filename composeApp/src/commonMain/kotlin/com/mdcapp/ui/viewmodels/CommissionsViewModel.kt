package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.CommissionConfig
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.entities.toEndOfDayMillis
import com.mdcapp.domain.entities.toStartOfDayMillis
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.commissionusecase.CommissionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CommissionsViewModel(
    private val getCommissions: CommissionUseCase.GetCommissions,
    private val repository: OrderRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    init {
        analytics.logScreenView("Commissions")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _commissionResults = _filterState.flatMapLatest { filters ->
        flow {
            if (filters.startDate != null && filters.endDate != null) {
                emit(getCommissions(filters.startDate, filters.endDate, filters.config))
            } else {
                emit(emptyList<CommissionUseCase.CommissionResult>())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state: StateFlow<UiState> = combine(
        _commissionResults,
        repository.observeFactories(),
        _filterState
    ) { results, factories, filters ->

        val items = results.mapNotNull { result ->
            val billing = result.billing
            val payment = result.payment

            val matchesFactory = if (filters.selectedFactory != null) {
                billing.brand.equals(filters.selectedFactory, ignoreCase = true)
            } else true

            val matchesSegment = if (filters.selectedSegment != null) {
                billing.branch.equals(filters.selectedSegment, ignoreCase = true)
            } else true

            val matchesType = if (filters.selectedType != null) {
                billing.type.contains(filters.selectedType, ignoreCase = true)
            } else true

            if (matchesFactory && matchesSegment && matchesType) {
                CommissionItem(payment, billing, result.commission)
            } else null
        }

        val allFactories = factories.map { it.name }.distinct().sorted()
        val allSegments = items.map { it.billing.branch }.distinct().sorted()
        val allTypes = listOf("Factura", "Remito")

        UiState(
            items = items.sortedByDescending { it.payment.date },
            totalCommission = items.sumOf { it.commissionAmount },
            factories = allFactories,
            segments = allSegments,
            types = allTypes,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState(isLoading = true))

    data class FilterState(
        val startDate: Long? = null,
        val endDate: Long? = null,
        val selectedFactory: String? = null,
        val selectedSegment: String? = null,
        val selectedType: String? = null,
        val config: CommissionConfig = CommissionConfig()
    )

    data class UiState(
        val items: List<CommissionItem> = emptyList(),
        val totalCommission: Double = 0.0,
        val factories: List<String> = emptyList(),
        val segments: List<String> = emptyList(),
        val types: List<String> = emptyList(),
        val isLoading: Boolean = false
    )

    data class CommissionItem(
        val payment: PaymentRegisterModel,
        val billing: BillingModel,
        val commissionAmount: Double
    )

    fun setDateRange(start: Long?, end: Long?) {
        val adjustedStart = start?.toStartOfDayMillis()
        val adjustedEnd = end?.toEndOfDayMillis()
        _filterState.update { it.copy(startDate = adjustedStart, endDate = adjustedEnd) }
    }

    fun setFactory(factoryName: String?) {
        _filterState.update { it.copy(selectedFactory = factoryName) }
    }

    fun setSegment(segment: String?) {
        _filterState.update { it.copy(selectedSegment = segment) }
    }

    fun setType(type: String?) {
        _filterState.update { it.copy(selectedType = type) }
    }

    fun toggleDeductIVA(deduct: Boolean) {
        _filterState.update { it.copy(config = it.config.copy(deductIVA = deduct)) }
    }
}

private fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> = this
