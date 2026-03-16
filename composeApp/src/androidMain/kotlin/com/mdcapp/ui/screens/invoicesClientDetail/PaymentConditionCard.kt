package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.discountToPrint
import com.mdcapp.ui.screens.invoicesClientDetail.common.CardHeader
import com.mdcapp.ui.screens.invoicesClientDetail.common.SectionCard

@Composable
fun PaymentConditionCard(
    billing: BillingModel,
    onToggle: () -> Unit
) {

    val text = billing.paymentCondition.ifEmpty { "Sin condición seleccionada" }

    SectionCard {

        CardHeader(
            title = "Condición de pago",
            actionText = "Seleccionar",
            onAction = onToggle
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium
            )

            if (billing.discount != 0.0)
                Text(
                    text = "%${billing.discount.discountToPrint()}",
                    style = MaterialTheme.typography.bodyMedium
                )
        }
    }
}