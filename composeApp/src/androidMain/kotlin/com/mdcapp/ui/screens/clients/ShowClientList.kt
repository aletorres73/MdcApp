package com.mdcapp.ui.screens.clients

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ClientModel
import com.mdcapp.ui.viewmodels.ClientsViewModel

@Composable
fun ShowClientList(
    clientList: List<ClientModel>,
    state: ClientsViewModel.UiState,
    listState: LazyListState,
    onItemClick: (id: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        state = listState
    ) {
        try {
            items(clientList) { client ->
                Card(
                    modifier = Modifier
                        .clickable { onItemClick(client.clientId) }
                        .fillMaxWidth()
                        .animateItem(),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(35.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(client.clientId, modifier = Modifier.weight(0.2f))
                        VerticalDivider(
                            modifier = Modifier
                                .size(16.dp)
                                .weight(0.2f)
                        )
                        Text(client.clientName, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (state.updatingData) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LazyColumn", "Error: ${e.message}")
        }
    }
}