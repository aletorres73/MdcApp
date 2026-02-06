package com.mdcapp.ui.composables.invoicesPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel

@Composable
fun InvoiceRow(invoice: BillingModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Fecha: ${invoice.loadDate}", style = MaterialTheme.typography.titleSmall)
            Text(invoice.clientName, style = MaterialTheme.typography.titleSmall)
            Text("Total: $${invoice.total}")
            Text("Estado: ${invoice.stateBilling}")
        }
    }
}
