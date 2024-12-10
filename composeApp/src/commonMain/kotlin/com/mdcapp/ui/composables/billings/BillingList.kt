package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel

@Composable
fun BillingList(
    billings: List<BillingModel>,
    onBillingClicked: (BillingModel) -> Unit
) {
    val modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(billings, key = { it.billingNumber }) { billing ->
            ElevatedCard(
                modifier = modifier
                    .clickable { onBillingClicked(billing) }
            ) {
                Column(
                    modifier = modifier
                ) {
                    RowInfoBilling(
                        modifier = modifier,
                        key = "N°:",
                        value = billing.billingNumber,
                    )
                    RowInfoBilling(
                        modifier = modifier,
                        key = "Importe:",
                        value = billing.total,
                    )
                    RowInfoBilling(
                        modifier = modifier,
                        key = "Fecha facturación:",
                        value = billing.loadDate
                    )
                    RowInfoBilling(
                        modifier = modifier,
                        key = "Fecha recepción:",
                        value = billing.deliveryDate
                    )
                    RowInfoBilling(
                        modifier = modifier,
                        key = "Fecha pago:",
                        value = billing.payDate
                    )
                }
            }

        }
    }
}

