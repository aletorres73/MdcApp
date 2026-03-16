package com.mdcapp.ui.composables.invoicePage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.toPrint

@Composable
fun InvoiceRow(
    invoice: BillingModel,
    onNavigationInvoice: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigationInvoice(invoice.billingNumber) },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                        text = "Documento #${invoice.billingNumber} • ${invoice.type}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = invoice.clientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )
                }
                Column {
                    Text(
                        text = invoice.total.toPrint(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (invoice.discount > 0)
                        Text(
                            text = "Dtos: $ ${invoice.discount.toPrint()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
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
                if (invoice.rest >= 0)
                    AmountLabel(
                        label = "Saldo",
                        value = invoice.rest,
                        color = Color.DarkGray
                    )

            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Fechas
            Text(
                text = "Carga: ${invoice.loadDate}  •  Entrega: ${invoice.deliveryDate}  •  Pago: ${invoice.payDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Marca
            if (invoice.brand.isNotBlank()) {
                Text(
                    text = invoice.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
            text = "$ ${value.toPrint()}",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}