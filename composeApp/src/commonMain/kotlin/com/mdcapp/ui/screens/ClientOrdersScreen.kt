package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.ui.viewmodels.orders.ClientOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ClientOrdersScreen(
    clientId: String,
    onBack: () -> Unit,
    onAddOrder: (String) -> Unit,
    onOrderClick: (String, String) -> Unit, // orderId, factoryName
    onAssignInvoice: (String) -> Unit,
    viewModel: ClientOrdersViewModel = koinViewModel(parameters = { parametersOf(clientId) })
) {
    val state by viewModel.state.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadOrders(clientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedidos del Cliente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { onAssignInvoice("") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Facturar sin pedido"
                        )
                    }
                    IconButton(onClick = { onAddOrder(clientId) }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Pedido")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Este cliente no tiene pedidos registrados.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                this.items(items = state.orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOrderClick(order.id, order.factory) }
                    ) {
                        ListItem(
                            headlineContent = { Text("Pedido N° ${order.id}") },
                            supportingContent = {
                                Column {
                                    Text("Fábrica: ${order.factory}")
                                    if (order.branch.isNotEmpty()) {
                                        Text(
                                            "Segmento: ${order.branch}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Text(
                                        "Artículos: ${order.articles.size}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        "Fecha: ${order.loadedDate.toFormattedDate()}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            trailingContent = {
                                Button(onClick = { onAssignInvoice(order.id) }) {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Facturar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
