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
import com.mdcapp.domain.entities.BuyOrderModel

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
        val styleTitle = MaterialTheme.typography.titleSmall
        val styleArticle = MaterialTheme.typography.bodySmall

        Column {
            Text(text = "Articulo", style = styleTitle)
            buyOrder.articles.forEach { article ->
                    Text(
                        text = article.name,
                        style = styleArticle
                    )
            }

        }
        Column {
            Text(text = "Color", style = styleTitle)
            buyOrder.articles.forEach { article ->
                    Text(
                        text = article.color,
                        style = styleArticle
                    )
            }
        }
        Column {
            Text(text = "P. Pedidos", style = styleTitle)
            buyOrder.articles.forEach { article ->
                    Text(
                        text = article.pairs.toString(),
                        style = styleArticle
                    )
            }
        }
        Column {
            Text(text = "P. Facturados", style = styleTitle)
            buyOrder.articles.forEach { article ->
                    Text(
                        text = article.delivered.toString(),
                        style = styleArticle
                    )
            }
        }
    }
}
