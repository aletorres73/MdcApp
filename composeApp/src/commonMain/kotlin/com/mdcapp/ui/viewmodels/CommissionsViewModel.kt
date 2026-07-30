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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CommissionsViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    val state: StateFlow<UiState> = combine(
        repository.observeAllBillings(),
        repository.observeAllPayments(),
        repository.observeFactories(),
        _filterState
    ) { billings, payments, factories, filters ->

        val filteredPayments = payments.filter { payment ->
            val inRange = if (filters.startDate != null && filters.endDate != null) {
                payment.date in filters.startDate..filters.endDate
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

                if (matchesFactory) {
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

        UiState(
            items = items.sortedByDescending { it.payment.date },
            totalCommission = items.sumOf { it.commissionAmount },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState(isLoading = true))

    data class FilterState(
        val startDate: Long? = null,
        val endDate: Long? = null,
        val selectedFactory: String? = null,
        val config: CommissionConfig = CommissionConfig()
    )

    data class UiState(
        val items: List<CommissionItem> = emptyList(),
        val totalCommission: Double = 0.0,
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

    fun toggleDeductIVA(deduct: Boolean) {
        _filterState.update { it.copy(config = it.config.copy(deductIVA = deduct)) }
    }
}

private fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> = this
