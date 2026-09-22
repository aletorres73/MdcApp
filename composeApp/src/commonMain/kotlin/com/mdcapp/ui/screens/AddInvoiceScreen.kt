package com.mdcapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.domain.entities.toEpochMillis
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toLocalDate
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.viewmodels.invoices.AddInvoiceViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun AddInvoiceScreen(
    clientId: String,
    orderId: String?,
    onBack: () -> Unit,
    viewModel: AddInvoiceViewModel = koinViewModel(parameters = { parametersOf(orderId ?: "") })
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(clientId, orderId) {
        viewModel.loadData(clientId, orderId ?: "")
    }
    var number by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var issueDate by remember { mutableLongStateOf(0L) }
    var deliveryDate by remember { mutableLongStateOf(0L) }
    var selectedCondition by remember { mutableStateOf<PaymentCondition?>(null) }
    var selectedType by remember { mutableStateOf("Factura") }

    LaunchedEffect(state.selectedCondition) {
        selectedCondition = state.selectedCondition
    }
    var notes by remember { mutableStateOf("") }

    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showDeliveryDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onBack()
        }
    }

    LaunchedEffect(state.scannedDocumentResult) {
        state.scannedDocumentResult?.let { result ->
            result["number"]?.let { if (it.isNotBlank()) number = it }
            result["total"]?.let { if (it.isNotBlank()) amount = it }
            result["type"]?.let { if (it.isNotBlank()) selectedType = it }
            result["date"]?.let { dateStr ->
                try {
                    issueDate = LocalDate.parse(dateStr).toEpochMillis()
                } catch (e: Exception) {
                    // Ignore parsing error
                }
            }
            viewModel.onScannedDocumentResultConsumed()
        }
    }

    if (showIssueDatePicker) {
        DatePicker(
            onDismissRequest = { showIssueDatePicker = false },
            onDateSelected = { millis ->
                issueDate = millis
                showIssueDatePicker = false
            },
            onDismissButton = { showIssueDatePicker = false },
            enable = true
        )
    }

    if (showDeliveryDatePicker) {
        DatePicker(
            onDismissRequest = { showDeliveryDatePicker = false },
            onDateSelected = { millis ->
                deliveryDate = millis
                showDeliveryDatePicker = false
            },
            onDismissButton = { showDeliveryDatePicker = false },
            enable = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear nueva facturación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.pendingDocument != null) {
                AssistChip(
                    onClick = { },
                    label = { Text("Documento adjunto: ${state.pendingDocument?.fileName}") },
                    leadingIcon = {
                        Icon(Icons.Filled.Info, contentDescription = "Documento")
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (state.scanningDocument) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Analizando documento adjunto con IA...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.orderId.isNotEmpty()) {
                Text("Pedido ID: ${state.orderId}", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    "Facturación sin pedido previo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text("Cliente: ${state.buyOrder.client}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            if (state.orderId.isEmpty()) {
                FactorySelector(
                    factories = state.factories,
                    selectedFactory = state.selectedFactory,
                    onFactorySelected = { viewModel.onFactorySelected(it) }
                )

                if (state.selectedFactory != null && state.selectedFactory!!.branchList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SegmentSelector(
                        segments = state.selectedFactory!!.branchList,
                        selectedSegment = state.selectedBranch,
                        onSegmentSelected = { viewModel.onBranchSelected(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Tipo de documento", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Factura", "Remito").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                    viewModel.onNumberChange(it)
                },
                label = { Text("Número de Factura / Remito") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.isExistingInvoice,
                supportingText = {
                    if (state.isExistingInvoice) {
                        Text(
                            "Este número ya existe. Si continúas, se actualizará la factura existente.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto Total") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = if (issueDate != 0L) issueDate.toFormattedDate() else "Sin especificar",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Fecha de Emisión") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIssueDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = if (deliveryDate != 0L) deliveryDate.toFormattedDate() else "Sin especificar",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Fecha de Recepción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeliveryDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConditionSelector(
                conditions = state.paymentConditionList,
                selected = selectedCondition,
                onSelected = { selectedCondition = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas de facturación") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val baseDate = if (deliveryDate != 0L) deliveryDate else issueDate
                    val payDate = if (baseDate != 0L && selectedCondition != null) {
                        baseDate.toLocalDate()
                            .plusDays(selectedCondition?.expiration?.toLong() ?: 0)
                            .toEpochMillis()
                    } else 0L
                    viewModel.saveInvoice(
                        number = number,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        condition = selectedCondition,
                        issueDate = issueDate,
                        deliveryDate = deliveryDate,
                        payDate = payDate,
                        type = selectedType,
                        notes = notes
                    )
                },
                enabled = !state.isLoading && number.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = if (state.isExistingInvoice) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(if (state.isExistingInvoice) "Actualizar Factura" else "Guardar Factura")
            }
        }
    }

    LoadingOverlay(state.isLoading)
}
