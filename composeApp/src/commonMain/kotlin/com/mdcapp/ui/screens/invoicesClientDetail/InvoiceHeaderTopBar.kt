package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.mdcapp.domain.entities.BillingModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHeaderTopBar(
    billing: BillingModel,
    onBack: () -> Unit,
    onDelete: () -> Unit = {}
) {
    TopAppBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        title = { InvoiceHeaderCard(billing = billing) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            // TODO: Implement multiplatform sharing
            /*IconButton(onClick = {
                val report = ReportGenerator.generateInvoiceReport(billing)
                ShareUtils.shareText(context, report, "Factura ${billing.billingNumber}")
            }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
            }*/
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    )
}
