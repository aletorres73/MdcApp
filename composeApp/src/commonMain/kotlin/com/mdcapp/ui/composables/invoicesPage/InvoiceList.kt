package com.mdcapp.ui.composables.invoicesPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel

@Composable
fun InvoiceList(
    invoices: List<BillingModel>,
    isLoading: Boolean,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(invoices) { index, invoice ->

            InvoiceRow(invoice)

            if (index == invoices.lastIndex && !isLoading) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        }
    }
}
