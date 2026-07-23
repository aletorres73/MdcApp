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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.PaymentCondition
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
    var editingFactory by remember { mutableStateOf<String?>(null) }

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
                editingFactory = null
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
                                IconButton(onClick = { viewModel.deleteFactory(factory.name) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
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
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>, List<PaymentCondition>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currentSegment by remember { mutableStateOf("") }
    val segments = remember { mutableStateListOf<String>() }
    // PaymentCondition simplified for now
    val conditions = remember { mutableStateListOf<PaymentCondition>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Fábrica") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Fábrica") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Segmentos", style = MaterialTheme.typography.titleSmall)
                Row {
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
                    Row {
                        Text(s, modifier = Modifier.weight(1f))
                        IconButton(onClick = { segments.removeAt(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Del"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Condiciones (Contado 0% por defecto)",
                    style = MaterialTheme.typography.titleSmall
                )
                // Simplified: adding a default condition if empty
                if (conditions.isEmpty()) {
                    Button(onClick = {
                        conditions.add(
                            PaymentCondition(
                                "Contado",
                                0.0,
                                0,
                                0,
                                0,
                                1
                            )
                        )
                    }) {
                        Text("Añadir Condición Base")
                    }
                }
                conditions.forEachIndexed { index, c ->
                    Text("${c.paymentName} - ${c.discount}%")
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
}
