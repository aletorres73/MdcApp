package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.viewmodels.ClientsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsListScreen(
    onClientClick: (String) -> Unit,
    onEditClientClick: (String, String) -> Unit,
    viewModel: ClientsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Forzar recarga de clientes al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading && state.clients.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.clients.isEmpty()) {
            Text(
                text = "No tienes clientes registrados.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.clients) { client ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onClientClick(client.clientId) }
                    ) {
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
                                Row {
                                    IconButton(onClick = {
                                        onEditClientClick(
                                            client.clientId,
                                            client.clientName
                                        )
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { viewModel.deleteClient(client.clientId) }) {
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
    }
}
