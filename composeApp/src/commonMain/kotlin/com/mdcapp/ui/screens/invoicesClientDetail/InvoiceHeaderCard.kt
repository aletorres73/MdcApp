package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel

@Composable
fun InvoiceHeaderCard(
    billing: BillingModel,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Número + Marca
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Factura Nº ${billing.billingNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(billing.clientName, style = MaterialTheme.typography.bodySmall)
                    Text(" - ", style = MaterialTheme.typography.bodySmall)
                    Text(billing.brand, style = MaterialTheme.typography.bodySmall)
                }
            }

            StateBadge(billing.stateBilling)
        }

        Spacer(Modifier.height(16.dp))
    }
}
