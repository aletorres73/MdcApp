package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(billings, key = { it.billingNumber }) { billing ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBillingClicked(billing) },
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                val columnModifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                val columnArrangement = Arrangement.spacedBy(6.dp)
                Column(
                    modifier = columnModifier,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = columnArrangement

                        ) {
                            RowInfoBilling(
                                key = "N°:",
                                value = billing.billingNumber
                            )
                            RowInfoBilling(
                                key = "Importe:",
                                value = if (billing.total.isNotEmpty())
                                    billing.total.replace(",", "")
                                else
                                    "$0.00"
                            )
                            RowInfoBilling(
                                key = "C.P:",
                                value = ""
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = columnArrangement
                        ) {
                            RowInfoBilling(
                                key = "Facturación:",
                                value = billing.loadDate
                            )
                            RowInfoBilling(
                                key = "Recepción:",
                                value = billing.deliveryDate
                            )
                            RowInfoBilling(
                                key = "Pago:",
                                value = billing.payDate
                            )
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = columnArrangement
                        ) {
                            RowInfoBilling(
                                key = "Dtos:",
                                value = "$0.00"
                            )
                            RowInfoBilling(
                                key = "A cobrar:",
                                value = "$0.00"
                            )
                            RowInfoBilling(
                                key = "Pagado:",
                                value = "$0.00"
                            )
                            RowInfoBilling(
                                key = "Saldo:",
                                value = "$0.00"
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val rowAlignment = Alignment.CenterVertically
                            val rowArrangement = Arrangement.spacedBy(4.dp)
                            InputChip(
                                onClick = {},
                                label = {
                                    Row(
                                        verticalAlignment = rowAlignment,
                                        horizontalArrangement = rowArrangement
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = null
                                        )
                                        Text("Agregar recepción")
                                    }
                                },
                                selected = true
                            )
                            InputChip(
                                onClick = {},
                                label = {
                                    Row(
                                        verticalAlignment = rowAlignment,
                                        horizontalArrangement = rowArrangement
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = null
                                        )
                                        Text("Condición de pago")
                                    }
                                },
                                selected = true
                            )
                            InputChip(
                                onClick = {},
                                label = {
                                    Row(
                                        verticalAlignment = rowAlignment,
                                        horizontalArrangement = rowArrangement
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = null
                                        )
                                        Text("Registrar pago")
                                    }
                                },
                                selected = true
                            )
                        }
                    }
                }
            }
        }
    }
}


