package com.mdcapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.viewmodels.CommissionsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun CommissionsScreen(
    showDatePickerRequest: MutableState<Boolean>,
    viewModel: CommissionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val filters by viewModel.filterState.collectAsState()

    var showDatePicker by showDatePickerRequest
    var showFilters by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with Filters Summary and Total
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Comisión Total: ${state.totalCommission.toPrint()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    val dateRangeText = if (filters.startDate != null && filters.endDate != null) {
                        "${filters.startDate!!.toFormattedDate()} - ${filters.endDate!!.toFormattedDate()}"
                    } else "Sin rango seleccionado"

                    Text(
                        dateRangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ocultar filtros",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(visible = showFilters) {
            Column {
                // Filtros Rápidos ( chips )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Fábrica:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = { viewModel.setFactory(null) },
                                label = { Text("Todas") },
                                colors = if (filters.selectedFactory == null) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                        items(state.factories) { factory ->
                            AssistChip(
                                onClick = { viewModel.setFactory(factory) },
                                label = { Text(factory) },
                                colors = if (filters.selectedFactory == factory) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                    }

                    Text("Segmento:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = { viewModel.setSegment(null) },
                                label = { Text("Todos") },
                                colors = if (filters.selectedSegment == null) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                        items(state.segments) { segment ->
                            AssistChip(
                                onClick = { viewModel.setSegment(segment) },
                                label = { Text(segment) },
                                colors = if (filters.selectedSegment == segment) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                    }

                    Text("Tipo:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = { viewModel.setType(null) },
                                label = { Text("Todos") },
                                colors = if (filters.selectedType == null) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                        items(state.types) { type ->
                            AssistChip(
                                onClick = { viewModel.setType(type) },
                                label = { Text(type) },
                                colors = if (filters.selectedType == type) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                    }
                }

                // IVA Configuration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Descontar IVA (21%)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Solo aplica a Facturas (no a Remitos)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = filters.config.deductIVA,
                        onCheckedChange = { viewModel.toggleDeductIVA(it) }
                    )
                }
            }
        }

        HorizontalDivider()

        // List of Commission Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.items) { item ->
                CommissionRow(
                    item = item,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateRange(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    showDatePicker = false
                }) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Seleccionar Rango", modifier = Modifier.padding(16.dp)) },
                headline = {
                    val start =
                        dateRangePickerState.selectedStartDateMillis?.toFormattedDate() ?: "Inicio"
                    val end = dateRangePickerState.selectedEndDateMillis?.toFormattedDate() ?: "Fin"
                    Text("$start - $end", modifier = Modifier.padding(horizontal = 16.dp))
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FactoryStatusBadge(timestamp: Long) {
    val isConfirmed = timestamp != 0L
    val text =
        if (isConfirmed) "Confirmado: ${timestamp.toFormattedDate()}" else "Pendiente Fábrica"
    val color = if (isConfirmed) Color(0xFF2E7D32) else Color(0xFFF9A825)

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp
        )
    }
}

@Composable
fun CommissionRow(
    item: CommissionsViewModel.CommissionItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.billing.clientName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${item.payment.date.toFormattedDate()} • ${item.payment.method}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            if (item.payment.notes.isNotBlank()) {
                Text(
                    "Ref: ${item.payment.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            FactoryStatusBadge(item.payment.confirmationTimestamp)
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                item.commissionAmount.toPrint(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Monto Pagado: ${item.payment.total.toPrint()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Documento N°: ${item.billing.billingNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Badge(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(item.billing.type)
            }
        }
    }
}
