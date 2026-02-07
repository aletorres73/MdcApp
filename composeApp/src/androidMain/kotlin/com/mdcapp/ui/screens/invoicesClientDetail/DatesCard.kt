package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel

@Composable
fun DatesCard(billing: BillingModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        val isTablet = LocalConfiguration.current.screenWidthDp > 600
        val arrangement = if (isTablet) Arrangement.SpaceBetween else Arrangement.spacedBy(12.dp)

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = arrangement
        ) {
            DateColumn("Fecha", billing.loadDate, Modifier.weight(1f))
            DateColumn("Recepción", billing.deliveryDate, Modifier.weight(1f))
            DateColumn("Pago", billing.payDate, Modifier.weight(1f))
        }
    }
}
