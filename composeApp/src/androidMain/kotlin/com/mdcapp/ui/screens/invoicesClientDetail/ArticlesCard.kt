package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.mdcapp.domain.entities.ArticleModel
import com.mdcapp.ui.composables.common.infotables.TableCell
import com.mdcapp.ui.composables.common.infotables.TableHeader

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
                    "Detalle facturación",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(onClick = onToggle) {
                    Text(if (expanded) "Ocultar detalle" else "Ver detalle")
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado tabla
                    Row(Modifier.fillMaxWidth()) {
                        TableHeader("Artículo", modifier = Modifier.weight(0.25f))
                        TableHeader("Color", modifier = Modifier.weight(0.35f))
                        TableHeader("Pares", modifier = Modifier.weight(0.2f))
                        TableHeader("Importe", modifier = Modifier.weight(0.2f))
                    }

                    HorizontalDivider()
                    // Filas artículos
                    articles.forEach { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            val align = TextAlign.Start
                            TableCell(a.name, modifier = Modifier.weight(0.25f), align)
                            TableCell(a.color, modifier = Modifier.weight(0.35f), align)
                            TableCell("${a.pairs}", modifier = Modifier.weight(0.2f), align)
                            TableCell("$${a.value}", modifier = Modifier.weight(0.2f))
                        }
                    }
                }
            }
        }
    }
}
