package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.toPrint

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentList(documents: List<BillingModel>, onInvoiceClick: (String) -> Unit) {
    val horizontalScroll = rememberScrollState()

    // 🔹 Ancho fijo por columna (ajústalos según tu diseño o proporciones reales)
    val colFecha = 95.dp
    val colNumero = 95.dp
    val colImporte = 120.dp
    val colPagado = 120.dp
    val colSaldo = 120.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScroll)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val style = MaterialTheme.typography.labelMedium
                    HeaderCell("Fecha", style, colFecha)
                    HeaderCell("Número", style, colNumero)
                    HeaderCell("Importe", style, colImporte, TextAlign.End)
                    HeaderCell("Pagado", style, colPagado, TextAlign.End)
                    HeaderCell("Saldo", style, colSaldo, TextAlign.End)
                }
                HorizontalDivider(thickness = 1.dp)
            }

            items(documents) { doc ->
                Row(
                    modifier = Modifier
                        .clickable { onInvoiceClick(doc.billingNumber) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val styleText = MaterialTheme.typography.bodySmall
                    DataCell(doc.loadDate, styleText, colFecha)
                    DataCell(doc.billingNumber, styleText, colNumero)
                    DataCell(doc.total.toPrint(), styleText, colImporte, TextAlign.End)
                    DataCell(doc.payed.toPrint(), styleText, colPagado, TextAlign.End)
                    DataCell(doc.rest.toPrint(), styleText, colSaldo, TextAlign.End)
                }
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}
