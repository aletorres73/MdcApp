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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ClientModel
import com.mdcapp.data.model.FactoryModel
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.ui.viewmodels.orders.CreateOrderViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    clientId: String? = null,
    onBack: () -> Unit,
    onManageFactories: () -> Unit = {},
    viewModel: CreateOrderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddArticleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(clientId) {
        viewModel.initData(clientId)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Pedido") },
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
            onDismiss = { showAddArticleDialog = false },
            onConfirm = { name, color, pairs ->
                viewModel.addArticle(name, color, pairs)
                showAddArticleDialog = false
            }
        )
    }
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
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var pairs by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Artículo") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artículo") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pairs,
                    onValueChange = { pairs = it },
                    label = { Text("Cantidad de Pares") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, color, pairs.toIntOrNull() ?: 0) }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
