package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.ui.screens.invoicesClientDetail.common.SectionCard

@Composable
fun DatesCard(
    billing: BillingModel,
    onReceptionDateClick: () -> Unit
) {

    SectionCard {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DateColumn("Fecha", billing.loadDate, Modifier.weight(1f))
            DateColumn(
                "Recepción",
                billing.deliveryDate,
                Modifier.weight(1f)
            ) { onReceptionDateClick() }
            DateColumn("Pago", billing.payDate, Modifier.weight(1f))

        }
    }
}