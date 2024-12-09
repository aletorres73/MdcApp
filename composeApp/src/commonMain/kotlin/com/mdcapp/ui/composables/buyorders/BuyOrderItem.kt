package com.mdcapp.ui.composables.buyorders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BuyOrderModel

@Composable
fun BuyOrderItem(buyOrder: BuyOrderModel) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val styleTitle = MaterialTheme.typography.titleMedium
        val styleArticle = MaterialTheme.typography.bodyMedium

        Column {
            Text(text = "Articulo", style = styleTitle)
            buyOrder.articles.forEach { article ->
                article["Articulo"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }

        }
        Column {
            Text(text = "Color", style = styleTitle)
            buyOrder.articles.forEach { article ->
                article["Color"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }
        }
        Column {
            Text(text = "Pares pedidos", style = styleTitle)
            buyOrder.articles.forEach { article ->
                article["Pares"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }
        }
        Column {
            Text(text = "Pares facturados", style = styleTitle)
            buyOrder.articles.forEach { article ->
                article["Entregados"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }
        }
    }
}