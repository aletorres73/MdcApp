package com.mdcapp.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toLocalDate
import com.mdcapp.domain.repositories.OrderRepository
import com.mdcapp.ui.theme.getBillingStatusColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class AgendaViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate

    val uiState: StateFlow<UiState> = combine(
        repository.observeAllBillings(),
        _selectedDate
    ) { billings, selectedDate ->

        // 1. Generar los días de la cinta semanal (14 días desde el inicio de la semana actual)
        val startOfWeek = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)
        val days = (0..13).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            DayState(
                date = date,
                indicators = getIndicatorsForDate(date, billings)
            )
        }

        // 2. Filtrar facturas para el día seleccionado
        val billingsOnSelectedDate = billings.filter { billing ->
            billing.payDate != 0L && billing.payDate.toLocalDate() == selectedDate
        }

        UiState(
            days = days,
            billingsOnSelectedDate = billingsOnSelectedDate,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState(isLoading = true)
    )

    data class UiState(
        val days: List<DayState> = emptyList(),
        val billingsOnSelectedDate: List<BillingModel> = emptyList(),
        val isLoading: Boolean = false
    )

    data class DayState(
        val date: LocalDate,
        val indicators: List<Color>
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun getIndicatorsForDate(date: LocalDate, billings: List<BillingModel>): List<Color> {
        val states = billings
            .filter { it.payDate != 0L && it.payDate.toLocalDate() == date && it.rest > 0 }
            .map { it.stateBilling }
            .distinct()

        return states.map { getBillingStatusColor(it) }
    }
}
