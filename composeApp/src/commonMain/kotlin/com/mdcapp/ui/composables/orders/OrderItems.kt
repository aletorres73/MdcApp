package com.mdcapp.ui.composables.orders

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
import com.mdcapp.data.model.OrderModel

@Composable
fun OrderItems(order: OrderModel) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "Orden ${order.orderNumber}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Pedido ${order.orders}", style = MaterialTheme.typography.bodyMedium)
            Text(text = order.nameClient, style = MaterialTheme.typography.bodyMedium)
        }
    }
}