package com.mdcapp.ui.composables.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.data.model.OrderModel

@Composable
fun OrderItems(order: OrderModel, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onCardClick)
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            val body = MaterialTheme.typography.bodyMedium
            Text(
                text = "Orden ${order.orderNumber}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = order.nameClient,
                style = body
            )
            val infoOrder = linkedMapOf(
                "Pedido: " to order.orders,
                "Estado de orden: " to order.payState,
                "Estado de entrega: " to order.trackingState,
            )
            infoOrder.forEach { (key, value) ->
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key,
                        style = body,
                    )
                    Text(
                        text = value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}