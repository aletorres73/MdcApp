package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate

@Composable
fun BillingItem(billing: BillingModel) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    Column(
        modifier = modifier
    ) {
        ArticlesDetailBilling(billing.articles, modifier)
        DetailBilling(
            modifier = modifier,
            order = billing.orderId,
            type = billing.type,
            total = billing.total.toString(),
            loadDate = billing.loadDate,
            deliveryDate = billing.deliveryDate,
            payDate = billing.payDate
        )
    }

}

@Composable
fun DetailBilling(
    modifier: Modifier = Modifier,
    order: String,
    type: String,
    total: String,
    loadDate: Long,
    deliveryDate: Long,
    payDate: Long
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Numero de orden:",
            value = order
        )
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Importe total:",
            value = total
        )
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Tipo de facturación:",
            value = type
        )
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Fecha facturación",
            value = loadDate.toFormattedDate()
        )
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Fecha recepción",
            value = deliveryDate.toFormattedDate()
        )
        RowInfoBilling(
            modifier = modifier.fillMaxWidth(),
            key = "Fecha pago:",
            value = payDate.toFormattedDate()
        )

    }
}


