package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel

@Composable
fun PaymentConditionCard(
    billing: BillingModel,
    onToggle: () -> Unit = {}
) {
    val text = "Sin condición seleccionada"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            // HEADER: Título + Botón expandir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Condición de pago", style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = onToggle
                ) { Text("Seleccionar condición") }
            }
            Text(
                billing.paymentCondition.ifEmpty { text },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
