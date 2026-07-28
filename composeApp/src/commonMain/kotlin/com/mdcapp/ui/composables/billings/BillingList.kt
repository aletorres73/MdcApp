package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint

@Composable
fun BillingList(
    billings: List<BillingModel>,
    onBillingClicked: (BillingModel) -> Unit,
    onAddPaymentCondition: (billingNumber: String) -> Unit,
    onAddDeliveryDate: (billingNumber: String) -> Unit,
    onPaymentRegister: (billingNumber: String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        items(billings, key = { it.billingNumber }) { billing ->
            ElevatedCard(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .clickable { onBillingClicked(billing) }
                    .padding(4.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                val columnModifier = Modifier.padding(horizontal = 12.dp).weight(1f)
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sección superior: Número de factura e importe
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(
                            modifier = columnModifier,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RowInfoBilling(
                                key = "N°:",
                                value = billing.billingNumber,
                                isTitle = true // Estilo de título
                            )
                            RowInfoBilling(
                                key = "Importe:",
                                value = billing.total.toPrint()
                            )
                            RowInfoBilling(
                                key = "C.P:",
                                value = billing.paymentCondition
                            )
                        }
                        Column(
                            modifier = columnModifier,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RowInfoBilling(
                                key = "Facturación:",
                                value = billing.loadDate.toFormattedDate()
                            )
                            RowInfoBilling(
                                key = "Recepción:",
                                value = billing.deliveryDate.toFormattedDate()
                            )
                            RowInfoBilling(
                                key = "Pago:",
                                value = billing.payDate.toFormattedDate()
                            )
                        }
                    }

                    // Divisor horizontal
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Sección inferior: Descuentos, pagos y saldos
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = columnModifier,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RowInfoBilling(
                                key = "Dtos:",
                                value = "$${billing.discount}"
                            )
                            RowInfoBilling(
                                key = "A cobrar:",
                                value = "$${billing.toPay}"
                            )
                            RowInfoBilling(
                                key = "Pagado:",
                                value = "$${billing.payed}"
                            )
                            RowInfoBilling(
                                key = "Saldo:",
                                value = "$${billing.rest}"
                            )
                        }
                        Column(
                            modifier = columnModifier,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillingInputChip(
                                onClick = { onAddDeliveryDate(billing.billingNumber) },
                                text = "Agregar recepción"
                            )
                            BillingInputChip(
                                onClick = { onAddPaymentCondition(billing.billingNumber) },
                                text = "Condición de pago"
                            )
                            BillingInputChip(
                                onClick = { onPaymentRegister(billing.billingNumber) },
                                text = "Registrar pago"
                            )
                        }
                    }
                }
            }
        }
    }
}



