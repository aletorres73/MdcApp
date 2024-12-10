package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ArticlesDetailBilling(
    billingArticles: List<HashMap<String, String>>,
    modifier: Modifier = Modifier
) {
    val styleTitle = MaterialTheme.typography.titleMedium
    val styleArticle = MaterialTheme.typography.bodyMedium
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Articulo", style = styleTitle)
            billingArticles.forEach { article ->
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
            billingArticles.forEach { article ->
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
            billingArticles.forEach { article ->
                article["Pares"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }
        }
        Column {
            Text(text = "Importe", style = styleTitle)
            billingArticles.forEach { article ->
                article["Importe"]?.let {
                    Text(
                        text = it,
                        style = styleArticle
                    )
                }
            }
        }
    }
}