package com.mdcapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.viewmodels.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectClientForDocumentDialog(
    clientsViewModel: ClientsViewModel,
    documentName: String,
    onDismiss: () -> Unit,
    onClientSelected: (String) -> Unit
) {
    val state by clientsViewModel.state.collectAsState()

    DisposableEffect(Unit) {
        clientsViewModel.setSearchMode(true)
        onDispose {
            clientsViewModel.clearQuery()
            clientsViewModel.setSearchMode(false)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Asignar Comprobante")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
                Text(
                    text = "Archivo recibido: $documentName\n¿A qué cliente pertenece?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { clientsViewModel.onQueryChange(it.text) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar cliente...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (state.searchQuery.text.isNotEmpty()) {
                            IconButton(onClick = { clientsViewModel.clearQuery() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.clients) { client ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClientSelected(client.clientId) }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = client.clientName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
