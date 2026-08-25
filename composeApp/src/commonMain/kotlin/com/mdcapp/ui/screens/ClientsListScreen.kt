package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.composables.common.RefreshContainer
import com.mdcapp.ui.viewmodels.ClientsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ClientsListScreen(
    onOrdersClick: (String) -> Unit,
    onCurrentAccountClick: (String) -> Unit,
    onEditClientClick: (String, String) -> Unit,
    viewModel: ClientsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var clientToDelete by remember { mutableStateOf<ClientModel?>(null) }

    /*
        // Forzar recarga de clientes al entrar a la pantalla
        LaunchedEffect(Unit) {
            viewModel.loadClients()
        }
    */
    Box(modifier = Modifier.fillMaxSize()) {
        RefreshContainer(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.loadClients() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.clients.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "No tienes clientes registrados.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.clients) { client ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            onClick = { onEditClientClick(client.clientId, client.clientName) }
                        ) {
                            Column {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = client.clientName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    supportingContent = { Text("ID: ${client.clientId}") },
                                    trailingContent = {
                                        IconButton(onClick = { clientToDelete = client }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onOrdersClick(client.clientId) }) {
                                        Text("PEDIDOS")
                                    }
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    TextButton(onClick = { onCurrentAccountClick(client.clientId) }) {
                                        Text("CTA. CTE.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        LoadingOverlay(state.isLoading)
    }

    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Eliminar Cliente") },
            text = {
                Text("¿Estás seguro de que deseas eliminar a ${client.clientName}? Se borrarán también de forma permanente todos sus pedidos, facturas y pagos asociados. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClient(client.clientId)
                        clientToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

}
