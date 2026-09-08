package com.mdcapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.ClientModel
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.composables.common.RefreshContainer
import com.mdcapp.ui.composables.common.SearchBar
import com.mdcapp.ui.utils.AppBackHandler
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

    if (state.isSearchMode) {
        AppBackHandler {
            viewModel.setSearchMode(false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.isSearchMode) {
                SearchBar(
                    query = state.searchQuery.text,
                    onQueryChange = { viewModel.onQueryChange(it) },
                    onCleanQuery = { viewModel.clearQuery() },
                    onBack = { viewModel.setSearchMode(false) },
                    searchText = "Buscar por nombre o ID..."
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !state.showInactive,
                    onClick = { viewModel.setShowInactive(false) },
                    label = { Text("Activos (${state.amountClients})") }
                )

                FilterChip(
                    selected = state.showInactive,
                    onClick = { viewModel.setShowInactive(true) },
                    label = { Text("Ver inactivos") },
                    leadingIcon = if (state.showInactive) {
                        {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }

            if (state.isLoading && state.clients.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }

            RefreshContainer(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.loadClients() },
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp), // Espacio para el FAB
                ) {
                    if (state.clients.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!state.isLoading) {
                                    Text(
                                        text = "No tienes clientes registrados.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = state.clients,
                            key = { it.clientId }
                        ) { client ->
                            val isClientActive = client.isActive
                            Column(
                                modifier = Modifier.alpha(if (isClientActive) 1f else 0.6f)
                            ) {
                                ListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEditClientClick(
                                                client.clientId,
                                                client.clientName
                                            )
                                        },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isClientActive) MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.6f
                                            ) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.4f
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    headlineContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = client.clientName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isClientActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (!isClientActive) {
                                                Spacer(Modifier.width(8.dp))
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = {
                                                        Text(
                                                            "Inactivo",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    border = null
                                                )
                                            }
                                        }
                                    },
                                    supportingContent = {
                                        Text(
                                            "ID: ${client.clientId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isClientActive) {
                                                IconButton(onClick = { onOrdersClick(client.clientId) }) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.List,
                                                        contentDescription = "Pedidos",
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            IconButton(onClick = { onCurrentAccountClick(client.clientId) }) {
                                                Icon(
                                                    Icons.Default.DateRange,
                                                    contentDescription = "Cuenta Corriente",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            if (isClientActive) {
                                                IconButton(onClick = { clientToDelete = client }) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Archivar",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else {
                                                IconButton(onClick = {
                                                    viewModel.restoreClient(client.clientId)
                                                }) {
                                                    Icon(
                                                        Icons.Default.Refresh,
                                                        contentDescription = "Restaurar",
                                                        tint = MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
        LoadingOverlay(state.isLoading && state.clients.isEmpty())
    }

    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Archivar Cliente") },
            text = {
                Text("¿Estás seguro de que deseas archivar a ${client.clientName}? Dejará de aparecer en las listas de selección para nuevos pedidos, pero se conservará todo su historial de facturas y pagos.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClient(client.clientId)
                        clientToDelete = null
                    }
                ) {
                    Text("Archivar", color = MaterialTheme.colorScheme.error)
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
