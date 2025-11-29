package com.mdcapp.ui.screens.invoices

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
import com.mdcapp.data.model.ArticleModel
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
                        TableHeader("Artículo", modifier = Modifier.weight(0.25f))
                        TableHeader("Color", modifier = Modifier.weight(0.25f))
                        TableHeader("Pares", modifier = Modifier.weight(0.25f))
                        TableHeader("Importe", modifier = Modifier.weight(0.25f))
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
                                TextAlign.Start
                            )
                            TableCell("$${a.value}", modifier = Modifier.weight(0.2f))
                        }
                    }
                }
            }
        }
    }
}
