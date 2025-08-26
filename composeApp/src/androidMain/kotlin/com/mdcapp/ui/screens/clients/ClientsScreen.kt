package com.mdcapp.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ClientModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen() {
    val clientList = (1..10).map {
        ClientModel(
            clientId = it.toString(),
            clientName = "Cliente $it"
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Clientes MDC")
                }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                value = "",
                onValueChange = {},
                placeholder = { Text(text = "Buscar cliente") },
                shape = RoundedCornerShape(4.dp),
                label = { Text(text = "Buscar cliente") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ID",
                    modifier = Modifier.weight(0.2f),
                    style = MaterialTheme.typography.titleMedium
                )
//                VerticalDivider(modifier = Modifier.size(16.dp).weight(0.2f))
                Text(
                    "Razón Social",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(clientList, key = { client -> client.clientId }) { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(35.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(client.clientId, modifier = Modifier.weight(0.2f))
                            VerticalDivider(modifier = Modifier
                                .size(16.dp)
                                .weight(0.2f))
                            Text(client.clientName, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun ClientsScreenPreview() {
    ClientsScreen()
}