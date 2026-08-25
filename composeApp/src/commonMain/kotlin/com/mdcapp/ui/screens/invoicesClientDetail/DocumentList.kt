package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toFormattedDate
import com.mdcapp.domain.entities.toPrint

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentList(
    documents: List<BillingModel>,
    invoicesWithPending: Set<String> = emptySet(),
    wideMode: Boolean = false,
    onInvoiceClick: (String) -> Unit
) {
    val horizontalScroll = rememberScrollState()

    // 🔹 Ancho fijo por columna (Solo para móvil)
    val colFecha = if (wideMode) 0.dp else 85.dp
    val colNumero = if (wideMode) 0.dp else 85.dp
    val colSegmento = if (wideMode) 0.dp else 85.dp
    val colImporte = if (wideMode) 0.dp else 100.dp
    val colPagado = if (wideMode) 0.dp else 100.dp
    val colSaldo = if (wideMode) 0.dp else 100.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!wideMode) Modifier.horizontalScroll(horizontalScroll) else Modifier)
        ) {
            val headerStyle =
                MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            val rowPadding = if (wideMode) 16.dp else 8.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(vertical = 12.dp, horizontal = rowPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell("Fecha", headerStyle, colFecha, weight = if (wideMode) 1.2f else null)
                HeaderCell("Número", headerStyle, colNumero, weight = if (wideMode) 1.2f else null)
                HeaderCell("Seg", headerStyle, colSegmento, weight = if (wideMode) 1f else null)
                HeaderCell(
                    "Neto",
                    headerStyle,
                    colImporte,
                    TextAlign.End,
                    weight = if (wideMode) 1.5f else null
                )
                HeaderCell(
                    "Pagado",
                    headerStyle,
                    colPagado,
                    TextAlign.End,
                    weight = if (wideMode) 1.5f else null
                )
                HeaderCell(
                    "Saldo",
                    headerStyle,
                    colSaldo,
                    TextAlign.End,
                    weight = if (wideMode) 1.5f else null
                )
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(documents) { doc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInvoiceClick(doc.billingNumber) }
                            .padding(horizontal = rowPadding, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val styleText = MaterialTheme.typography.bodyMedium
                        DataCell(
                            doc.loadDate.toFormattedDate(),
                            styleText,
                            colFecha,
                            weight = if (wideMode) 1.2f else null
                        )

                        Row(
                            modifier = if (wideMode) Modifier.weight(1.2f) else Modifier.width(
                                colNumero
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.billingNumber,
                                style = styleText,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (invoicesWithPending.contains(doc.billingNumber)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Pendiente Imputar",
                                    tint = Color(0xFFF9A825),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DataCell(
                            doc.branch,
                            styleText,
                            colSegmento,
                            weight = if (wideMode) 1f else null
                        )
                        DataCell(
                            doc.toPay.toPrint(),
                            styleText,
                            colImporte,
                            TextAlign.End,
                            weight = if (wideMode) 1.5f else null
                        )
                        DataCell(
                            doc.payed.toPrint(),
                            styleText,
                            colPagado,
                            TextAlign.End,
                            weight = if (wideMode) 1.5f else null,
                            color = if (doc.payed > 0) Color(0xFF2E7D32) else null
                        )
                        DataCell(
                            doc.rest.toPrint(),
                            styleText.copy(fontWeight = FontWeight.Bold),
                            colSaldo,
                            TextAlign.End,
                            weight = if (wideMode) 1.5f else null,
                            color = if (doc.rest > 0) MaterialTheme.colorScheme.error else null
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.HeaderCell(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start,
    weight: Float? = null
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        modifier = if (weight != null) Modifier.weight(weight) else Modifier.width(width)
    )
}

@Composable
fun RowScope.DataCell(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start,
    weight: Float? = null,
    color: Color? = null
) {
    Text(
        text = text,
        style = if (color != null) style.copy(color = color) else style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = if (weight != null) Modifier.weight(weight) else Modifier.width(width)
    )
}
