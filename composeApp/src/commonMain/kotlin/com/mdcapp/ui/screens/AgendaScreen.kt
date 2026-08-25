package com.mdcapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.theme.getBillingStatusColor
import com.mdcapp.ui.viewmodels.AgendaViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel = koinViewModel(),
    onInvoiceClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 800.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Panel Izquierdo: Calendario y Alertas
                Column(
                    modifier = Modifier
                        .width(350.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Text(
                        "Calendario",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    WeeklyCalendarWide(
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.onDateSelected(it) },
                        days = state.days,
                        weekRangeText = state.weekRangeText,
                        onPreviousWeek = { viewModel.previousWeek() },
                        onNextWeek = { viewModel.nextWeek() },
                        onTodayClick = { viewModel.goToToday() }
                    )

                    Spacer(Modifier.height(24.dp))

                    if (state.urgentBillingsCount > 0) {
                        UrgentBillingsAlert(
                            count = state.urgentBillingsCount,
                            isFiltering = state.isFilteringUrgent,
                            onClick = { viewModel.toggleUrgentFilter() }
                        )
                    }
                }

                VerticalDivider(thickness = 0.5.dp)

                // Panel Derecho: Lista de Vencimientos
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    AgendaHeader(selectedDate, state.isFilteringUrgent) {
                        viewModel.toggleUrgentFilter()
                    }

                    AgendaContent(state, onInvoiceClick)
                }
            }
        } else {
            // Layout Móvil (Original)
            Column(modifier = Modifier.fillMaxSize()) {
                WeeklyCalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.onDateSelected(it) },
                    days = state.days,
                    weekRangeText = state.weekRangeText,
                    onPreviousWeek = { viewModel.previousWeek() },
                    onNextWeek = { viewModel.nextWeek() },
                    onTodayClick = { viewModel.goToToday() }
                )

                HorizontalDivider()

                if (state.urgentBillingsCount > 0) {
                    UrgentBillingsAlert(
                        count = state.urgentBillingsCount,
                        isFiltering = state.isFilteringUrgent,
                        onClick = { viewModel.toggleUrgentFilter() }
                    )
                }

                AgendaHeader(selectedDate, state.isFilteringUrgent) {
                    viewModel.toggleUrgentFilter()
                }

                AgendaContent(state, onInvoiceClick)
            }
        }
    }
}

@Composable
fun AgendaHeader(
    selectedDate: LocalDate,
    isFilteringUrgent: Boolean,
    onToggleUrgent: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isFilteringUrgent) {
                "Todos los Vencimientos Urgentes"
            } else {
                "Vencimientos del ${
                    selectedDate.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                            "dd 'de' MMMM",
                            Locale("es")
                        )
                    )
                }"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        if (isFilteringUrgent) {
            Text(
                "Cerrar",
                modifier = Modifier.clickable { onToggleUrgent() },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun AgendaContent(
    state: AgendaViewModel.UiState,
    onInvoiceClick: (String) -> Unit
) {
    if (state.billingsOnSelectedDate.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.isFilteringUrgent) "No hay vencimientos urgentes." else "No hay vencimientos para esta fecha.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.billingsOnSelectedDate) { billing ->
                AgendaItemRow(
                    billing = billing,
                    onClick = { onInvoiceClick(billing.billingNumber) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
fun WeeklyCalendarWide(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    days: List<AgendaViewModel.DayState>,
    weekRangeText: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }
                Text(weekRangeText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(onClick = onNextWeek) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(days) { dayState ->
                    DayItem(
                        date = dayState.date,
                        isSelected = dayState.date == selectedDate,
                        onClick = { onDateSelected(dayState.date) },
                        indicators = dayState.indicators
                    )
                }
            }

            TextButton(
                onClick = onTodayClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Ir a Hoy", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun WeeklyCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    days: List<AgendaViewModel.DayState>,
    weekRangeText: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Semana anterior"
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weekRangeText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Hoy",
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .clickable { onTodayClick() }
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onNextWeek) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Semana siguiente"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { dayState ->
                DayItem(
                    date = dayState.date,
                    isSelected = dayState.date == selectedDate,
                    onClick = { onDateSelected(dayState.date) },
                    indicators = dayState.indicators
                )
            }
        }
    }
}

@Composable
fun UrgentBillingsAlert(count: Int, isFiltering: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isFiltering) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            contentColor = if (isFiltering) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                if (isFiltering) Icons.Default.Info else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isFiltering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = if (isFiltering) {
                    "Viendo todos los vencimientos urgentes ($count)"
                } else {
                    "Atención: Tienes $count vencimientos urgentes o atrasados. Toca para verlos."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    indicators: List<Color>
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(55.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            indicators.take(3).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun AgendaItemRow(billing: BillingModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                billing.clientName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${billing.brand} - ${billing.billingNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(billing.type, fontSize = 9.sp)
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                billing.rest.toPrint(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = getBillingStatusColor(billing.stateBilling)
            )
            Text(
                billing.stateBilling,
                style = MaterialTheme.typography.labelSmall,
                color = getBillingStatusColor(billing.stateBilling)
            )
        }
    }
}
