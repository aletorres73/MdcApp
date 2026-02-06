package com.mdcapp.ui.composables.invoicePage

import androidx.compose.desktop.ui.tooling.preview.Preview
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

@Composable
fun InvoiceRow(invoice: BillingModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = "$ ${invoice.total}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (invoice.discount > 0)
                        Text(
                            text = "Dtos: $ ${invoice.discount}",
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
            text = "$ $value",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Preview
@Composable
fun invoiceRowPreview() {
    val invoice = BillingModel(
        billingNumber = "123456",
        orderId = "123456",
        type = "Factura",
        total = 100.0,
        loadDate = "2023-04-01",
        deliveryDate = "2023-04-02",
        payDate = "2023-04-03",
        articles = emptyList(),
        paymentCondition = "Contado",
        discount = 0.0,
        toPay = 100.0,
        payed = 100.0,
        rest = 0.0,
        stateBilling = "Pendiente",
        clientId = "123456",
        brand = "Adidas",
        comments = emptyList(),
        clientName = "Adidas",
        timeStamp = 123456
    )
    InvoiceRow(invoice)

}
