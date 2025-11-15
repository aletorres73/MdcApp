package com.mdcapp.ui.composables.billings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mdcapp.data.model.ArticleModel

@Composable
fun ArticlesDetailBilling(
    billingArticles: List<ArticleModel>,
    modifier: Modifier = Modifier
) {
    val styleTitle = MaterialTheme.typography.titleMedium
    val styleArticle = MaterialTheme.typography.bodyMedium

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Columna Artículo
        Column {
            Text(text = "Artículo", style = styleTitle)
            billingArticles.forEach { article ->
                Text(
                    text = article.name,
                    style = styleArticle
                )
            }
        }

        // Columna Color
        Column {
            Text(text = "Color", style = styleTitle)
            billingArticles.forEach { article ->
                Text(
                    text = article.color,
                    style = styleArticle
                )
            }
        }

        // Columna Pares pedidos
        Column {
            Text(text = "Pares pedidos", style = styleTitle)
            billingArticles.forEach { article ->
                Text(
                    text = article.pairs.toString(),
                    style = styleArticle
                )
            }
        }

        // Columna Entregados
        Column {
            Text(text = "Entregados", style = styleTitle)
            billingArticles.forEach { article ->
                Text(
                    text = article.delivered.toString(),
                    style = styleArticle
                )
            }
        }
    }
}
