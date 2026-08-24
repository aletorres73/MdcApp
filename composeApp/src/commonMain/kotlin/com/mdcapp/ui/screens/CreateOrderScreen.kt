package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.viewmodels.orders.CreateOrderViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun CreateOrderScreen(
    clientId: String? = null,
    orderId: String? = null,
    onBack: () -> Unit,
    onManageFactories: () -> Unit = {},
    viewModel: CreateOrderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddArticleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(clientId, orderId) {
        viewModel.initData(clientId, orderId)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onBack()
        }
    }

    LaunchedEffect(state.isDialogSuccess) {
        if (state.isDialogSuccess) {
            showAddArticleDialog = false
            viewModel.resetDialog()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.orderId != null) "Editar Pedido" else "Nuevo Pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onManageFactories) {
                        Icon(Icons.Default.Settings, contentDescription = "Gestionar Fábricas")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Selección de Cliente
            ClientSelector(
                clients = state.clients,
                selectedClient = state.selectedClient,
                onClientSelected = { viewModel.onClientSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selección de Fábrica
            FactorySelector(
                factories = state.factories,
                selectedFactory = state.selectedFactory,
                onFactorySelected = { viewModel.onFactorySelected(it) }
            )

            if (state.selectedFactory != null && state.selectedFactory!!.branchList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SegmentSelector(
                    segments = state.selectedFactory!!.branchList,
                    selectedSegment = state.selectedSegment,
                    onSegmentSelected = { viewModel.onSegmentSelected(it) }
                )
            }

            if (state.selectedFactory != null && state.selectedFactory!!.paymentType.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                ConditionSelector(
                    conditions = state.selectedFactory!!.paymentType,
                    selected = state.selectedCondition,
                    onSelected = { viewModel.onConditionSelected(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Artículos", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddArticleDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(state.articles) { index, article ->
                    ListItem(
                        headlineContent = { Text(article.name) },
                        supportingContent = { Text("Color: ${article.color} - Pares: ${article.pairs}") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.removeArticle(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.comments,
                onValueChange = { viewModel.onCommentsChange(it) },
                label = { Text("Notas de venta / Comentarios") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.saveOrder() },
                enabled = !state.isLoading && state.articles.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Pedido")
            }
        }
    }

    if (showAddArticleDialog) {
        AddArticleDialog(
            state = state,
            onNameChange = { viewModel.onDialogNameChange(it) },
            onColorChange = { viewModel.onDialogColorChange(it) },
            onPairsChange = { viewModel.onDialogPairsChange(it) },
            onIncrement = { viewModel.incrementPairs() },
            onDecrement = { viewModel.decrementPairs() },
            onClearName = { viewModel.onDialogNameChange("") },
            onClearColor = { viewModel.onDialogColorChange("") },
            onConfirm = {
                viewModel.addArticle(
                    state.dialogArticleName,
                    state.dialogArticleColor,
                    state.dialogArticlePairs.toIntOrNull() ?: 0
                )
            },
            onConfirmAndContinue = { viewModel.addArticleAndContinue() },
            onDismiss = {
                showAddArticleDialog = false
                viewModel.resetDialog()
            }
        )
    }

    LoadingOverlay(state.isLoading)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientSelector(
    clients: List<ClientModel>,
    selectedClient: ClientModel?,
    onClientSelected: (ClientModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedClient?.clientName ?: "Seleccionar Cliente",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cliente") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.clientName) },
                    onClick = {
                        onClientSelected(client)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactorySelector(
    factories: List<FactoryModel>,
    selectedFactory: FactoryModel?,
    onFactorySelected: (FactoryModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedFactory?.name ?: "Seleccionar Fábrica",
            onValueChange = {},
            readOnly = true,
            label = { Text("Fábrica") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            factories.forEach { factory ->
                DropdownMenuItem(
                    text = { Text(factory.name) },
                    onClick = {
                        onFactorySelected(factory)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentSelector(
    segments: List<String>,
    selectedSegment: String,
    onSegmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = if (selectedSegment.isEmpty()) "Seleccionar Segmento" else selectedSegment,
            onValueChange = {},
            readOnly = true,
            label = { Text("Segmento / Marca") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            segments.forEach { segment ->
                DropdownMenuItem(
                    text = { Text(segment) },
                    onClick = {
                        onSegmentSelected(segment)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionSelector(
    conditions: List<PaymentCondition>,
    selected: PaymentCondition?,
    onSelected: (PaymentCondition?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.paymentName ?: "Opcional: Condición de Pago",
            onValueChange = {},
            readOnly = true,
            label = { Text("Condición de Pago") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ninguna (Opcional)") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            conditions.forEach { condition ->
                DropdownMenuItem(
                    text = { Text("${condition.paymentName} (-${(condition.discount * 100).toInt()}%)") },
                    onClick = {
                        onSelected(condition)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddArticleDialog(
    state: CreateOrderViewModel.UiState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onPairsChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onConfirm: () -> Unit,
    onConfirmAndContinue: () -> Unit,
    onDismiss: () -> Unit,
    onClearName: () -> Unit,
    onClearColor: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Artículo") },
        text = {
            Column {
                if (state.dialogError != null) {
                    Text(
                        text = state.dialogError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = state.dialogArticleName,
                    onValueChange = onNameChange,
                    label = { Text("Artículo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.dialogError != null && state.dialogArticleName.isBlank(),
                    trailingIcon = {
                        if (state.dialogArticleName.isNotEmpty()) {
                            IconButton(onClick = onClearName) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.dialogArticleColor,
                    onValueChange = onColorChange,
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.dialogError != null && state.dialogArticleColor.isBlank(),
                    trailingIcon = {
                        if (state.dialogArticleColor.isNotEmpty()) {
                            IconButton(onClick = onClearColor) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cantidad de Pares", style = MaterialTheme.typography.labelMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDecrement) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    OutlinedTextField(
                        value = state.dialogArticlePairs,
                        onValueChange = onPairsChange,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = state.dialogError != null && (state.dialogArticlePairs.toIntOrNull()
                            ?: 0) <= 0,
                        textStyle = TextStyle(textAlign = TextAlign.Center)
                    )
                    IconButton(onClick = onIncrement) {
                        Icon(Icons.Default.Add, contentDescription = "Más")
                    }
                }
                Text(
                    "Múltiplos de 12 (Docena)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onConfirmAndContinue) {
                    Text("Continuar")
                }
                Button(onClick = onConfirm) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

