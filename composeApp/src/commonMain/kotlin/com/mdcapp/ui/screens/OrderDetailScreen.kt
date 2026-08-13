package com.mdcapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.ArticleOrderModel
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.ui.theme.getBillingStatusColor
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun OrderDetailScreen(
    clientId: String,
    orderId: String,
    factoryName: String,
    onBack: () -> Unit,
    onNavigateToInvoice: (String) -> Unit,
    onEditOrder: (String, String) -> Unit,
    viewModel: BuyOrdersViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(clientId, orderId, factoryName) {
        viewModel.init(clientId, orderId, factoryName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Pedido N° $orderId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (!state.loadingOrder) {
                        IconButton(onClick = {
                            onEditOrder(clientId, orderId)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Pedido")
                        }
                        // TODO: Implement multiplatform sharing
                        /*IconButton(onClick = {
                            val report = ReportGenerator.generateOrderReport(state.buyOrder)
                            ShareUtils.shareText(
                                context,
                                report,
                                "Nota de Pedido N° ${state.buyOrder.order}"
                            )
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }*/
                    }
                }
            )
        }
    ) { padding ->
        if (state.loadingOrder || state.loadingBillings) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OrderSummaryCard(
                    state.buyOrder.client,
                    state.buyOrder.factory,
                    state.buyOrder.branch,
                    state.buyOrder.loadedDate
                )

                ArticlesListCard(state.buyOrder.articles)

                InvoicesListCard(state.billings, onNavigateToInvoice)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OrderSummaryCard(client: String, factory: String, branch: String, dateMillis: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(Icons.Default.Person, "Cliente", client, isTitle = false)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            InfoRow(Icons.Default.Info, "Fábrica", factory)
            if (branch.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                InfoRow(Icons.Default.Info, "Segmento", branch)
            }
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.AutoMirrored.Filled.List, "Fecha Carga", dateMillis.toFormattedDate())
        }
    }
}

@Composable
fun ArticlesListCard(articles: List<ArticleOrderModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.2f
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Artículos Pedidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            if (articles.isEmpty()) {
                Text("No hay artículos registrados.", style = MaterialTheme.typography.bodyMedium)
            } else {
                articles.forEach { article ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                article.pairs.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(article.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Color: ${article.color}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (article != articles.last()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoicesListCard(billings: List<BillingModel>, onNavigateToInvoice: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Facturas Vinculadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            if (billings.isEmpty()) {
                Text(
                    "Este pedido no tiene facturas asignadas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                billings.forEach { billing ->
                    ListItem(
                        modifier = Modifier
                            .clickable { onNavigateToInvoice(billing.billingNumber) },
                        headlineContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("N° ${billing.billingNumber}")
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        billing.type
                                    )
                                }
                            }
                        },
                        supportingContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    billing.stateBilling,
                                    color = getBillingStatusColor(billing.stateBilling),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Pagado: ${billing.payed.toPrint()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Saldo: ${billing.rest.toPrint()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            Text(
                                billing.total.toPrint(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        }
                    )
                    if (billing != billings.last()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, isTitle: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isTitle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = if (isTitle) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isTitle) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
