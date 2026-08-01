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

    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)
    )

    private val _isFilteringUrgent = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        repository.observeAllBillings(),
        _selectedDate,
        _currentWeekStart,
        _isFilteringUrgent
    ) { billings, selectedDate, weekStart, isFilteringUrgent ->

        // 1. Generar los días de la semana (7 días desde el lunes seleccionado)
        val days = (0..6).map { i ->
            val date = weekStart.plusDays(i.toLong())
            DayState(
                date = date,
                indicators = getIndicatorsForDate(date, billings)
            )
        }

        // 2. Filtrar facturas
        val filteredBillings = if (isFilteringUrgent) {
            billings.filter {
                it.rest > 0 && (it.stateBilling == "Vencido" || it.stateBilling == "Por vencer")
            }.sortedBy { it.payDate }
        } else {
            billings.filter { billing ->
                billing.payDate != 0L && billing.payDate.toLocalDate() == selectedDate
            }
        }

        // 3. Detectar vencimientos urgentes generales
        val urgentBillingsCount = billings.count {
            it.rest > 0 && (it.stateBilling == "Vencido" || it.stateBilling == "Por vencer")
        }

        UiState(
            days = days,
            billingsOnSelectedDate = filteredBillings,
            urgentBillingsCount = urgentBillingsCount,
            weekRangeText = getWeekRangeText(weekStart),
            isFilteringUrgent = isFilteringUrgent,
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
        val urgentBillingsCount: Int = 0,
        val weekRangeText: String = "",
        val isFilteringUrgent: Boolean = false,
        val isLoading: Boolean = false
    )

    data class DayState(
        val date: LocalDate,
        val indicators: List<Color>
    )

    fun onDateSelected(date: LocalDate) {
        _isFilteringUrgent.value = false
        _selectedDate.value = date
    }

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        _selectedDate.value = _currentWeekStart.value
    }

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
        _selectedDate.value = _currentWeekStart.value
    }

    fun goToToday() {
        val today = LocalDate.now()
        _currentWeekStart.value = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        _selectedDate.value = today
        _isFilteringUrgent.value = false
    }

    fun toggleUrgentFilter() {
        _isFilteringUrgent.value = !_isFilteringUrgent.value
    }

    private fun getWeekRangeText(start: LocalDate): String {
        val end = start.plusDays(6)
        val formatter =
            java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale("es"))
        return "${start.format(formatter)} - ${end.format(formatter)}"
    }

    private fun getIndicatorsForDate(date: LocalDate, billings: List<BillingModel>): List<Color> {
        val states = billings
            .filter { it.payDate != 0L && it.payDate.toLocalDate() == date && it.rest > 0 }
            .map { it.stateBilling }
            .distinct()

        return states.map { getBillingStatusColor(it) }
    }
}
