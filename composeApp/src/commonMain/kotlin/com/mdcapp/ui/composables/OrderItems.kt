package com.mdcapp.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.Order

@Composable
fun OrderItems(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "Orden ${order.order}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Pedido ${order.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = order.client, style = MaterialTheme.typography.bodyMedium)
        }
    }
}