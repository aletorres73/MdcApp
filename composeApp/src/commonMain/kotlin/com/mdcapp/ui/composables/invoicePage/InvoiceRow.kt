package com.mdcapp.ui.composables.invoicePage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.theme.getBillingStatusColor

@Composable
fun InvoiceRow(
    invoice: BillingModel,
    hasPendingReconciliation: Boolean = false,
    onNavigationInvoice: (String) -> Unit = {}
) {
    val statusColor = rememberStatusColor(invoice)

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigationInvoice(invoice.billingNumber) },
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = statusColor,
                        size = Size(width = 6.dp.toPx(), height = size.height)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Client Name + Total Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = invoice.clientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = invoice.type,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = "N° ${invoice.billingNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hasPendingReconciliation) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Pendiente Imputar",
                                    tint = Color(0xFFF9A825),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = invoice.total.toPrint(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (invoice.expectedDiscount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "${(invoice.expectedDiscount * 100).toInt()}% Dto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Financial State
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (invoice.toPay > 0) {
                        AmountLabel(
                            label = "A cobrar",
                            value = invoice.toPay,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    AmountLabel(
                        label = "Pagado",
                        value = invoice.payed,
                        color = Color(0xFF2E7D32)
                    )
                    AmountLabel(
                        label = "Saldo",
                        value = invoice.rest,
                        color = if (invoice.rest > 0) MaterialTheme.colorScheme.error else Color.Gray,
                        isBold = invoice.rest > 0
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                // Footer: Dates and Brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateText = buildString {
                        append("Carga: ${invoice.loadDate.toFormattedDate()}")
                        if (invoice.payDate != 0L) {
                            append(" • Venc: ${invoice.payDate.toFormattedDate()}")
                        }
                    }
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (invoice.brand.isNotBlank()) {
                        Text(
                            text = invoice.brand,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberStatusColor(invoice: BillingModel): Color {
    return remember(invoice.rest, invoice.payDate, invoice.stateBilling) {
        getBillingStatusColor(invoice.stateBilling)
    }
}

@Composable
private fun AmountLabel(
    label: String,
    value: Double,
    color: Color,
    isBold: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toPrint(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

