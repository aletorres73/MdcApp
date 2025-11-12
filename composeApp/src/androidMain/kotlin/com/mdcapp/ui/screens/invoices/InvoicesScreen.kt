package com.mdcapp.ui.screens.invoices

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun InvoicesScreen(
    clientId: String,
    vm: InvoicesViewModel = koinViewModel(),
    onNavigate: () -> Unit
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.init(clientId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cliente: ${state.client.clientName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigate()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { /*TODO*/ },
                        label = { Text(text = "Marcas") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Marcas"
                            )
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingIndicator(true)
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            else -> {
                DetailClientBalance(paddingValues, state.documents)
            }
        }

    }
}

@Composable
fun DetailClientBalance(paddingValues: PaddingValues, documents: List<BillingModel>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) { Text("Saldo total de cuenta:", style = MaterialTheme.typography.titleMedium) }

            HorizontalDivider()
            Text("Estado de cuenta", style = MaterialTheme.typography.titleMedium)

            DocumentList(documents)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentList(documents: List<BillingModel>) {
    val horizontalScroll = rememberScrollState()

    // 🔹 Ancho fijo por columna (ajústalos según tu diseño o proporciones reales)
    val colFecha = 85.dp
    val colNumero = 65.dp
    val colImporte = 110.dp
    val colPagado = 110.dp
    val colSaldo = 110.dp

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
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val styleText = MaterialTheme.typography.bodySmall
                    DataCell(doc.loadDate, styleText, colFecha)
                    DataCell(doc.billingNumber, styleText, colNumero)
                    DataCell(doc.total, styleText, colImporte, TextAlign.End)
                    DataCell(doc.payed.ifBlank { "$0.00" }, styleText, colPagado, TextAlign.End)
                    DataCell(doc.rest.ifBlank { "$0.00" }, styleText, colSaldo, TextAlign.End)
                }
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    style: TextStyle,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun DataCell(
    text: String,
    style: TextStyle,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}
