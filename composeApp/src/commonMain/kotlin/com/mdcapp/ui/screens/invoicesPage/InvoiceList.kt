package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.ui.composables.invoicePage.InvoiceRow

@Composable
fun InvoiceList(
    invoices: List<BillingModel>,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onNavigationInvoice: (String) -> Unit,
    invoicesWithPending: Set<String> = emptySet()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (invoices.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron facturas.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = invoices,
                    key = { it.billingNumber }
                ) { invoice ->

                    InvoiceRow(
                        invoice = invoice,
                        onNavigationInvoice = { onNavigationInvoice(it) },
                        hasPendingReconciliation = invoicesWithPending.contains(invoice.billingNumber)
                    )

                    if (invoices.lastOrNull()?.billingNumber == invoice.billingNumber && !isLoading) {
                        LaunchedEffect(invoice.billingNumber) {
                            onLoadMore()
                        }
                    }
                }
            }
        }
    }
}
