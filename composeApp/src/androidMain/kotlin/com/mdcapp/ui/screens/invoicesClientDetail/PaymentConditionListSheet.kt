package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.PaymentCondition

@Composable
fun PaymentConditionListSheet(
    list: List<PaymentCondition>,
    factory: String = "",
    onSelect: (PaymentCondition) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        Header(factory)

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(list) { item ->

                PaymentConditionCard(
                    item = item,
                    onClick = { onSelect(item) }
                )

            }
        }
    }
}

@Composable
private fun Header(factory: String) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            "Condiciones de Pago",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            factory,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentConditionCard(
    item: PaymentCondition,
    onClick: () -> Unit
) {

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {

        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                item.paymentName,
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Metric("Descuento", "${(item.discount * 100).toInt()}%")
                Metric("Plazo", "${item.month} m")
                Metric("Venc.", "${item.expiration} d")

            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            value,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}