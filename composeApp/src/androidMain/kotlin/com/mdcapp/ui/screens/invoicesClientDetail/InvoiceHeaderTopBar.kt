package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.logic.ReportGenerator
import com.mdcapp.ui.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHeaderTopBar(
    billing: BillingModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    TopAppBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        title = { InvoiceHeaderCard(billing = billing) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = {
                val report = ReportGenerator.generateInvoiceReport(billing)
                ShareUtils.shareText(context, report, "Factura ${billing.billingNumber}")
            }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
            }
        }
    )
}

