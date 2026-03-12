package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mdcapp.data.model.BillingModel
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

        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )

    }
}