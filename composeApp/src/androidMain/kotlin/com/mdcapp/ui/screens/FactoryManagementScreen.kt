package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.ui.viewmodels.FactoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactoryManagementScreen(
    onBack: () -> Unit,
    viewModel: FactoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFactory by remember { mutableStateOf<FactoryModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Fábricas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedFactory = null
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Fábrica")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.factories) { factory ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(factory.name) },
                        supportingContent = { Text("${factory.branchList.size} Segmentos - ${factory.paymentType.size} Condiciones") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    selectedFactory = factory
                                    showAddDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { viewModel.deleteFactory(factory.name) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddFactoryDialog(
            initialFactory = selectedFactory,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, segments, conditions ->
                viewModel.saveFactory(name, segments, conditions)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddFactoryDialog(
    initialFactory: FactoryModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>, List<PaymentCondition>) -> Unit
) {
    var name by remember { mutableStateOf(initialFactory?.name ?: "") }
    var currentSegment by remember { mutableStateOf("") }
    val segments = remember {
        mutableStateListOf<String>().apply {
            initialFactory?.branchList?.let { addAll(it) }
        }
    }
    val conditions = remember {
        mutableStateListOf<PaymentCondition>().apply {
            initialFactory?.paymentType?.let { addAll(it) }
        }
    }

    var showConditionDialog by remember { mutableStateOf<PaymentCondition?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialFactory == null) "Nueva Fábrica" else "Editar Fábrica") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Fábrica") },
                    enabled = initialFactory == null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Segmentos", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = currentSegment,
                        onValueChange = { currentSegment = it },
                        label = { Text("Nuevo Segmento") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (currentSegment.isNotBlank()) {
                            segments.add(currentSegment)
                            currentSegment = ""
                        }
                    }) { Icon(Icons.Default.Add, contentDescription = "Add") }
                }
                segments.forEachIndexed { index, s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s, modifier = Modifier.weight(1f))
                        IconButton(onClick = { segments.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Del")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Condiciones de Pago", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showConditionDialog = PaymentCondition() }) {
                        Text("+ Agregar")
                    }
                }

                conditions.forEachIndexed { index, c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.paymentName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${(c.discount * 100).toInt()}% dto - ${c.expiration} días",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        IconButton(onClick = { showConditionDialog = c }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { conditions.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Del")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, segments.toList(), conditions.toList()) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showConditionDialog != null) {
        AddEditConditionDialog(
            initialCondition = if (showConditionDialog?.paymentName?.isEmpty() == true) null else showConditionDialog,
            onDismiss = { showConditionDialog = null },
            onConfirm = { updatedCondition ->
                val existingIndex =
                    conditions.indexOfFirst { it.paymentName == showConditionDialog?.paymentName }
                if (existingIndex != -1 && showConditionDialog?.paymentName?.isNotEmpty() == true) {
                    conditions[existingIndex] = updatedCondition
                } else {
                    conditions.add(updatedCondition)
                }
                showConditionDialog = null
            }
        )
    }
}

@Composable
fun AddEditConditionDialog(
    initialCondition: PaymentCondition? = null,
    onDismiss: () -> Unit,
    onConfirm: (PaymentCondition) -> Unit
) {
    var name by remember { mutableStateOf(initialCondition?.paymentName ?: "") }
    var discountPct by remember {
        mutableStateOf(initialCondition?.let {
            (it.discount * 100).toInt().toString()
        } ?: "")
    }
    var days by remember { mutableStateOf(initialCondition?.expiration?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCondition == null) "Nueva Condición" else "Editar Condición") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej: Pronto Pago)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = discountPct,
                    onValueChange = { discountPct = it },
                    label = { Text("Descuento (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Días de Plazo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    PaymentCondition(
                        paymentName = name,
                        discount = (discountPct.toDoubleOrNull() ?: 0.0) / 100.0,
                        expiration = days.toIntOrNull() ?: 0,
                        quantity = 1 // Default
                    )
                )
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

