package com.mdcapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import kotlinx.coroutines.launch

class HomeViewModel(private val getFactories: HomeUseCase.GetAllFactories) : ViewModel() {
    var state by mutableStateOf(UiState())

    data class UiState(
        val loading: Boolean = false,
        val error: Boolean = false,
        val factoryList: List<FactoryModel> = emptyList()
    )

    init {
        viewModelScope.launch { fetchFromRepository() }
    }

    private suspend fun fetchFromRepository() {
        state = state.copy(loading = true)
        val factoryList = getFactories()
        state = state.copy(
            loading = false,
            error = factoryList.isNotEmpty(),
            factoryList = factoryList.sortedByDescending { it.branchList.size }
        )
    }

}
