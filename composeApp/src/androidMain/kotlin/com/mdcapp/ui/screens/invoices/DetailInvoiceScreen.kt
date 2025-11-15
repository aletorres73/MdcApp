package com.mdcapp.ui.screens.invoices

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ArticleModel
import com.mdcapp.data.model.BillingModel


@Composable
fun DetailInvoiceScreen(
    billing: BillingModel,
    modifier: Modifier = Modifier
) {
    var showArticles by remember { mutableStateOf(true) }

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
                showArticles = showArticles,
                onToggleArticles = { showArticles = !showArticles }
            )

            // Fechas
            DatesCard(billing = billing)

            // Totales
            TotalsCard(billing = billing)

            // Condición de pago
            PaymentConditionCard(billing = billing)

            // Artículos (expandible)
            ArticlesCard(
                articles = billing.articles,
                expanded = showArticles
            )
        }
    }
}

@Composable
fun InvoiceHeaderCard(
    billing: BillingModel,
    showArticles: Boolean,
    onToggleArticles: () -> Unit
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

            // Botón expandir artículos
            TextButton(onClick = onToggleArticles) {
                Text(if (showArticles) "Ocultar artículos" else "Ver artículos")
            }
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
            TotalRow("Total", billing.total)
            TotalRow("Descuento", billing.discount.toString())
            TotalRow("A cobrar", billing.toPay.toString())
            TotalRow("Pagado", billing.payed)
            TotalRow("Saldo", billing.rest)
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
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ArticlesCard(
    articles: List<ArticleModel>,
    expanded: Boolean
) {
    AnimatedVisibility(visible = expanded) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Detalle artículos en documento",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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
                            .padding(vertical = 4.dp)
                    ) {
                        TableCell(a.name, modifier = Modifier.weight(0.35f))
                        TableCell(a.color, modifier = Modifier.weight(0.25f))
                        TableCell(a.pairs.toString(), modifier = Modifier.weight(0.2f))
                        TableCell(a.value, modifier = Modifier.weight(0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
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






