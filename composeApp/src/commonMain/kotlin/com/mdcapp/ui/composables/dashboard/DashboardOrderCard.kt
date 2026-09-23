package com.mdcapp.ui.composables.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdcapp.domain.entities.BuyOrderModel
import com.mdcapp.domain.entities.toFormattedDate

val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun DashboardOrderCard(
    order: BuyOrderModel,
    onOrderClick: (BuyOrderModel) -> Unit,
    onWhatsAppClick: (BuyOrderModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOrderClick(order) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${order.order.ifEmpty { order.id }}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val dateMillis = if (order.deliveryDate > 0) order.deliveryDate
                    else if (order.loadedDate > 0) order.loadedDate
                    else order.timeStamp

                    if (dateMillis > 0) {
                        Text(
                            text = dateMillis.toFormattedDate(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = order.client.ifEmpty { "Sin Cliente" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                if (order.factory.isNotBlank()) {
                    Text(
                        text = "Fábrica: ${order.factory} ${if (order.branch.isNotBlank()) "(${order.branch})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                val totalPairs = order.articles.sumOf { it.pairs }
                if (totalPairs > 0) {
                    Text(
                        text = "$totalPairs pares cargados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badge de estado genérico de la orden
                Surface(
                    shape = RoundedCornerShape(50),
                    color = WhatsAppGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (order.billing.isNotBlank()) "Facturado" else "Cargado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1B5E20)
                    )
                }

                // Botón WhatsApp
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen.copy(alpha = 0.15f))
                        .clickable { onWhatsAppClick(order) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Compartir WhatsApp",
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
