package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.domain.service.AnalyticsService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FactoryViewModel(
    private val repository: OrderRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    val state: StateFlow<UiState> = combine(
        repository.observeFactories(),
        _isLoading,
        _error,
        _message
    ) { factories, loading, err, msg ->
        UiState(
            isLoading = loading,
            factories = factories.sortedBy { it.name },
            error = err,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(isLoading = true)
    )

    data class UiState(
        val isLoading: Boolean = false,
        val factories: List<FactoryModel> = emptyList(),
        val error: String? = null,
        val message: String? = null
    )

    init {
        analytics.logScreenView("Factories")
    }

    fun loadFactories() {
        // Obsoleto con Flow
    }

    fun saveFactory(
        name: String,
        segments: List<String>,
        conditions: List<PaymentCondition>,
        defaultCommission: Double,
        segmentCommissions: Map<String, Double>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val factory = FactoryModel(
                    name = name,
                    branchList = segments,
                    paymentType = conditions,
                    defaultCommission = defaultCommission,
                    segmentCommissions = segmentCommissions
                )
                val success = repository.saveFactory(factory)
                if (success) {
                    analytics.logEvent("save_factory_success", mapOf("factory_name" to name))
                    _message.value = "Fábrica guardada"
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _error.value = "Error al guardar"
                }
            } catch (e: Exception) {
                Napier.e("Error saving factory", e)
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun deleteFactory(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = repository.deleteFactory(name)
                if (success) {
                    analytics.logEvent("delete_factory_success", mapOf("factory_name" to name))
                    _message.value = "Fábrica eliminada"
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _error.value = "Error al eliminar"
                }
            } catch (e: Exception) {
                Napier.e("Error deleting factory", e)
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
