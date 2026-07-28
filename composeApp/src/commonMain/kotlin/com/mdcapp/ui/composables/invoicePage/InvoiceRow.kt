package com.mdcapp.ui.composables.invoicePage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.theme.getBillingStatusColor

@Composable
fun InvoiceRow(
    invoice: BillingModel,
    onNavigationInvoice: (String) -> Unit = {}
) {
    val statusColor = rememberStatusColor(invoice)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigationInvoice(invoice.billingNumber) },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Semáforo Indicator
            Surface(
                modifier = Modifier.width(8.dp).fillMaxHeight(),
                color = statusColor
            ) {}

            Column(
                modifier = Modifier.padding(12.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Header: Cliente + Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Doc #${invoice.billingNumber} • ${invoice.type}",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = invoice.clientName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = invoice.total.toPrint(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (invoice.expectedDiscount > 0)
                            Text(
                                text = "Dto: ${(invoice.expectedDiscount * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                    }
                }

                // Estado de pagos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (invoice.toPay > 0)
                        AmountLabel(
                            label = "A pagar",
                            value = invoice.toPay,
                            color = Color(0xFFC62828)
                        )
                    AmountLabel(
                        label = "Pagado",
                        value = invoice.payed,
                        color = Color(0xFF2E7D32)
                    )
                    AmountLabel(
                        label = "Saldo",
                        value = invoice.rest,
                        color = if (invoice.rest > 0) Color(0xFFC62828) else Color.Gray
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Fechas
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val dates = mutableListOf<String>()
                    dates.add("Carga: ${invoice.loadDate.toFormattedDate()}")
                    if (invoice.deliveryDate != 0L) {
                        dates.add("Entrega: ${invoice.deliveryDate.toFormattedDate()}")
                    }
                    if (invoice.payDate != 0L) {
                        dates.add("Venc: ${invoice.payDate.toFormattedDate()}")
                    }

                    Text(
                        text = dates.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Marca
                if (invoice.brand.isNotBlank()) {
                    Text(
                        text = invoice.brand,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
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
    color: Color
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
            color = color
        )
    }
}

