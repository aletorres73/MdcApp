package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.CommissionConfig
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.MovementMethod
import com.mdcapp.domain.entities.PaymentRegisterModel
import com.mdcapp.domain.logic.CommissionCalculator
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.service.AnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CommissionsViewModel(
    private val repository: OrderRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    init {
        analytics.logScreenView("Commissions")
    }

    val state: StateFlow<UiState> = combine(
        repository.observeAllBillings(),
        repository.observeAllPayments(),
        repository.observeFactories(),
        _filterState
    ) { billings, payments, factories, filters ->

        val filteredPayments = payments.filter { payment ->
            val inRange = if (filters.startDate != null && filters.endDate != null) {
                // Ajustar el fin del día para incluir los pagos del último día seleccionado
                val adjustedEndDate = filters.endDate + (24 * 60 * 60 * 1000) - 1
                payment.date in filters.startDate..adjustedEndDate
            } else true

            val isRealPayment = !MovementMethod.fromName(payment.method).isVirtual

            inRange && isRealPayment
        }

        val items = filteredPayments.mapNotNull { payment ->
            // Buscar la factura asociada al pago para obtener los metadatos (Sucursal, Tipo, Fábrica)
            val billing = billings.find { it.billingNumber == payment.documentNumber }

            if (billing != null) {
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
                    // Buscar configuración de fábrica (insensible a mayúsculas para asegurar match)
                    val factory =
                        factories.find { it.name.equals(billing.brand, ignoreCase = true) }
                            ?: FactoryModel(billing.brand, emptyList(), emptyList())

                    val commission = CommissionCalculator.calculate(
                        amount = payment.total,
                        factory = factory,
                        branch = billing.branch,
                        docType = billing.type,
                        config = filters.config
                    )

                    CommissionItem(payment, billing, commission)
                } else null
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
        _filterState.update { it.copy(startDate = start, endDate = end) }
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
