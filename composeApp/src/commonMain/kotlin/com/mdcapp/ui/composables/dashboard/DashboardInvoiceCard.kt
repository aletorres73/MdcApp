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
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.theme.AccentAmberContainer
import com.mdcapp.ui.theme.WhatsAppGreen
import com.mdcapp.ui.theme.getBillingStatusColor

@Composable
fun DashboardInvoiceCard(
    invoice: BillingModel,
    onInvoiceClick: (BillingModel) -> Unit,
    onWhatsAppClick: (BillingModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = getBillingStatusColor(invoice.stateBilling)

    Card(
        onClick = { onInvoiceClick(invoice) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentAmberContainer
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
                        text = "#${invoice.billingNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val dateMillis =
                        if (invoice.loadDate > 0) invoice.loadDate else invoice.timeStamp
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
                    text = invoice.clientName.ifEmpty { "Cliente" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                if (invoice.brand.isNotBlank()) {
                    Text(
                        text = "Marca: ${invoice.brand}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = invoice.total.toPrint(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Semáforo Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = invoice.stateBilling.ifEmpty { "Pendiente" },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = statusColor
                        )
                    }

                    // Botón WhatsApp
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen.copy(alpha = 0.15f))
                            .clickable { onWhatsAppClick(invoice) },
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
}
