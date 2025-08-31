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

    private val _statusScreen = MutableStateFlow<ClientScreenStatus>(ClientScreenStatus.Idle)
    val statusScreen: StateFlow<ClientScreenStatus> = _statusScreen.asStateFlow()

    sealed class ClientScreenStatus {
        data object Idle : ClientScreenStatus()

        //        data object Loading: ClientScreenStatus()
        data class Error(val message: String) : ClientScreenStatus()
    }


    data class UiState(
        val data: List<ClientModel> = emptyList(),
        val updatingData: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null,
    )

    init {
        getClientsUseCase.resetPagination()
        loadNextPage()
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
                _statusScreen.value = ClientScreenStatus.Idle
            } catch (e: Exception) {
                _state.update { it.copy(updatingData = false, error = e.message) }
                _statusScreen.value = ClientScreenStatus.Error(e.message ?: "Error")
            }
        }
    }
}