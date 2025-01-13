package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    val listModifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)

    LazyColumn(
//        modifier = listModifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(billings, key = { it.billingNumber }) { billing ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable { onBillingClicked(billing) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RowInfoBilling(
                        modifier = Modifier,
                        key = "N°:",
                        value = billing.billingNumber,
                    )
                    RowInfoBilling(
                        modifier = Modifier,
                        key = "Importe:",
                        value = billing.total.replace(",", ""),
                    )
                    RowInfoBilling(
                        modifier = Modifier,
                        key = "Fecha facturación:",
                        value = billing.loadDate
                    )
                    RowInfoBilling(
                        modifier = Modifier,
                        key = "Fecha recepción:",
                        value = billing.deliveryDate
                    )
                    RowInfoBilling(
                        modifier = Modifier,
                        key = "Fecha pago:",
                        value = billing.payDate
                    )
                }
            }
        }
    }
}

