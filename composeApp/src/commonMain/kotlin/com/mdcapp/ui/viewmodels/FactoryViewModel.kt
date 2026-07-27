package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FactoryViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val factories: List<FactoryModel> = emptyList(),
        val error: String? = null,
        val message: String? = null
    )

    init {
        loadFactories()
    }

    fun loadFactories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val factories = repository.getFactories()
                _state.update { it.copy(isLoading = false, factories = factories) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun saveFactory(name: String, segments: List<String>, conditions: List<PaymentCondition>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val factory = FactoryModel(name, segments, conditions)
                val success = repository.saveFactory(factory)
                if (success) {
                    _state.update { it.copy(message = "Fábrica guardada") }
                    loadFactories()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error al guardar") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteFactory(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val success = repository.deleteFactory(name)
                if (success) {
                    _state.update { it.copy(message = "Fábrica eliminada") }
                    loadFactories()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error al eliminar") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

