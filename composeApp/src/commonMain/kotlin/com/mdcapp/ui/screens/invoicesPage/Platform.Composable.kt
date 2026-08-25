package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.runtime.Composable
import com.mdcapp.domain.entities.BillingModel

@Composable
expect fun DetailClientBalance(
    documents: List<BillingModel>,
    balance: Double,
    pendingAmount: Double,
    invoicesWithPending: Set<String>,
    brandSelected: String,
    branchSelected: String,
    typeSelected: String,
    brands: List<String>,
    branches: List<String>,
    types: List<String>,
    isWideScreen: Boolean = false,
    onBrandSelect: (String) -> Unit,
    onBranchSelect: (String) -> Unit,
    onTypeSelect: (String) -> Unit,
    onInvoiceClick: (String) -> Unit
)