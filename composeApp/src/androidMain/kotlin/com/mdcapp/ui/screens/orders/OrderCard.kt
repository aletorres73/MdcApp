package com.mdcapp.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.ArticleOrderModel
import com.mdcapp.data.model.BuyOrderModel
import com.mdcapp.ui.composables.common.infotables.TableCell
import com.mdcapp.ui.composables.common.infotables.TableHeader

@Composable
fun OrderCard(
    order: BuyOrderModel,
    expanded: Boolean,
    onToggle: () -> Unit
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
                    "Pedido relacionado",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(onClick = onToggle) {
                    Text(if (expanded) "Ocultar pedido" else "Ver pedido")
                }
            }

            // CONTENIDO EXPANDIBLE
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Datos principales del pedido
                    OrderInfoSection(order)

                    // Tabla de artículos del pedido
                    OrderArticlesTable(order.articles)

                    // Comentarios
                    HorizontalDivider()
                    if (order.comments.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Comentarios",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                order.comments,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoSection(order: BuyOrderModel) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

        InfoRow("Pedido Nº", order.order)
        InfoRow("Cliente", order.client)
        InfoRow("Marca", order.branch)
        InfoRow("Tipo", order.type)
        InfoRow("Entrega", order.deliveryDate)
        InfoRow("Cargado", order.loadedDate)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value)
    }
}

@Composable
fun OrderArticlesTable(articles: List<ArticleOrderModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
/*
        Text(
            "Artículos del pedido",
            style = MaterialTheme.typography.titleMedium
        )*/
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Encabezado
        Row(Modifier.fillMaxWidth()) {
            TableHeader("Artículo", Modifier.weight(0.35f))
            TableHeader("Color", Modifier.weight(0.25f))
            TableHeader("Pares", Modifier.weight(0.2f))
            TableHeader("Entregados", Modifier.weight(0.2f))
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()

        // LISTA
        articles.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                TableCell(item.name, Modifier.weight(0.25f), TextAlign.Start)
                TableCell(item.color, Modifier.weight(0.35f), TextAlign.Start)
                TableCell(item.pairs.toString(), Modifier.weight(0.2f), TextAlign.Center)
                TableCell(item.delivered.toString(), Modifier.weight(0.2f), TextAlign.Center)
            }
        }
    }
}


