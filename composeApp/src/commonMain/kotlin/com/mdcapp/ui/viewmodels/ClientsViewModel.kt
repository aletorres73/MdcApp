package com.mdcapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.data.model.ClientModel
import com.mdcapp.domain.usescases.clientsusecase.GetClientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _statusScreen = MutableStateFlow<ClientScreenStatus>(ClientScreenStatus.Idle())
    val statusScreen: StateFlow<ClientScreenStatus> = _statusScreen.asStateFlow()

    sealed class ClientScreenStatus {
        data class Idle(val message: String = "") : ClientScreenStatus()
        data object Search : ClientScreenStatus()
    }


    data class UiState(
        val data: List<ClientModel> = emptyList(),
        val amountClients: Long = 0,
        val dataSearch: List<ClientModel> = emptyList(),
        val updatingData: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    )

    init {
        getClientsUseCase.resetPagination()
        loadNextPage()
        getNumberOfClients()
    }

    private fun getNumberOfClients() {
        viewModelScope.launch {
            try {
                val amountClients = getClientsUseCase.getAmountClients()
                _state.update { it.copy(amountClients = amountClients) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadNextPage() {
        if (!_state.value.hasMore || _state.value.updatingData) return

        viewModelScope.launch {
            _state.update { it.copy(updatingData = true) }
            try {
                val (newData, hasMore) = getClientsUseCase()
                val data = _state.value.data.toMutableList()
                data.addAll(newData)
                val updateData = data.toList()

                _state.update {
                    it.copy(
                        data = updateData,
                        hasMore = hasMore,
                        updatingData = false
                    )
                }
                _statusScreen.value = ClientScreenStatus.Idle()
            } catch (e: Exception) {
                _state.update { it.copy(updatingData = false, error = e.message) }
                _statusScreen.value = ClientScreenStatus.Idle("Error al cargar")
            }
        }
    }

    fun searchClients(query: String) {
        viewModelScope.launch {
            try {
                if (query.isNotEmpty()) {
                    val data = getClientsUseCase.search(query)
                    if (data.isEmpty())
                        _statusScreen.value =
                            ClientScreenStatus.Idle("No se encontraron resultados")
                    else {
                        _state.update { it.copy(dataSearch = data) }
                        _statusScreen.value = ClientScreenStatus.Search
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetView() {
        getClientsUseCase.resetPagination()
        loadNextPage()
    }

}