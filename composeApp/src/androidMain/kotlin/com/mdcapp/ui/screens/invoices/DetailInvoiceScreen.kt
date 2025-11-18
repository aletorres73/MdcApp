package com.mdcapp.ui.screens.invoices

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ArticleModel
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.ui.composables.common.infotables.TableCell
import com.mdcapp.ui.composables.common.infotables.TableHeader
import com.mdcapp.ui.screens.orders.OrderCard


@Composable
fun DetailInvoiceScreen(
    billing: BillingModel,
    order: BuyOrderModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var showArticles by remember { mutableStateOf(false) }
    var showOrder by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    Scaffold { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Encabezado
            InvoiceHeaderCard(
                billing = billing,
            )

            // Fechas
            DatesCard(billing = billing)

            // Totales
            TotalsCard(billing = billing)

            // Condición de pago
            PaymentConditionCard(billing = billing)

            //Pedido
            OrderCard(
                order = order,
                expanded = showOrder,
                onToggle = { showOrder = !showOrder }
            )

            // Artículos (expandible)
            ArticlesCard(
                articles = billing.articles,
                expanded = showArticles,
                onToggle = { showArticles = !showArticles }
            )
        }
    }
}

@Composable
fun InvoiceHeaderCard(
    billing: BillingModel,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Número + Marca
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Factura Nº ${billing.billingNumber}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(billing.brand, style = MaterialTheme.typography.bodyMedium)
                }

                StateBadge(billing.stateBilling)
            }

            Spacer(Modifier.height(16.dp))

            /*            // Botón expandir artículos
                        TextButton(onClick = onToggleArticles) {
                            Text(if (showArticles) "Ocultar artículos" else "Ver artículos")
                        }*/
        }
    }
}

@Composable
fun StateBadge(state: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = state,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun DatesCard(billing: BillingModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        val isTablet = LocalConfiguration.current.screenWidthDp > 600
        val arrangement = if (isTablet) Arrangement.SpaceBetween else Arrangement.spacedBy(12.dp)

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = arrangement
        ) {
            DateColumn("Fecha", billing.loadDate, Modifier.weight(1f))
            DateColumn("Recepción", billing.deliveryDate, Modifier.weight(1f))
            DateColumn("Pago", billing.payDate, Modifier.weight(1f))
        }
    }
}

@Composable
fun DateColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TotalsCard(billing: BillingModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TotalRow("Total", billing.total.toString())
            TotalRow("Descuento", billing.discount.toString())
            TotalRow("A cobrar", (billing.total - billing.discount).toString())
            TotalRow("Pagado", billing.payed.toString())
            TotalRow("Saldo", billing.rest.toString())
        }
    }
}

@Composable
fun TotalRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text("$$value", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ArticlesCard(
    articles: List<ArticleModel>,
    expanded: Boolean,
    onToggle: () -> Unit = {},
) {
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
            ) {
                Text(
                    "Detalle articulos facturados",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(onClick = onToggle) {
                    Text(if (expanded) "Ocultar detalle" else "Ver detalle")
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado tabla
                    Row(Modifier.fillMaxWidth()) {
                        TableHeader("Artículo", modifier = Modifier.weight(0.35f))
                        TableHeader("Color", modifier = Modifier.weight(0.25f))
                        TableHeader("Pares", modifier = Modifier.weight(0.2f))
                        TableHeader("Importe", modifier = Modifier.weight(0.2f))
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    // Filas artículos
                    articles.forEach { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            TableCell(a.name, modifier = Modifier.weight(0.25f), TextAlign.Start)
                            TableCell(a.color, modifier = Modifier.weight(0.35f), TextAlign.Start)
                            TableCell(
                                "${a.pairs}",
                                modifier = Modifier.weight(0.2f),
                                TextAlign.Center
                            )
                            TableCell("$${a.value}", modifier = Modifier.weight(0.2f))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PaymentConditionCard(billing: BillingModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Condición de pago", style = MaterialTheme.typography.titleMedium)
            Text(billing.paymentCondition, style = MaterialTheme.typography.bodyMedium)
        }
    }
}






