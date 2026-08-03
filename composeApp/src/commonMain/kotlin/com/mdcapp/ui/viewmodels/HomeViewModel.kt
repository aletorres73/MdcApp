package com.mdcapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.service.AnalyticsService
import com.mdcapp.domain.usescases.homeusescases.HomeUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getFactories: HomeUseCase.GetAllFactories,
    private val analytics: AnalyticsService
) : ViewModel() {
    var state by mutableStateOf(UiState())

    data class UiState(
        val loading: Boolean = false,
        val error: Boolean = false,
        val factoryList: List<FactoryModel> = emptyList()
    )

    init {
        analytics.logScreenView("Home")
        viewModelScope.launch { fetchFromRepository() }
    }

    private suspend fun fetchFromRepository() {
        state = state.copy(loading = true)
        try {
            val factoryList = getFactories()
            state = state.copy(
                loading = false,
                error = factoryList.isNotEmpty(),
                factoryList = factoryList.sortedByDescending { it.branchList.size }
            )
        } catch (e: Exception) {
            Napier.e("Error fetching factories in Home", e)
            state = state.copy(loading = false, error = true)
        }
    }
}
