package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.ui.screens.invoicesClientDetail.BalanceCard
import com.mdcapp.ui.screens.invoicesClientDetail.DocumentList
import com.mdcapp.ui.screens.invoicesClientDetail.FilterSection

@Composable
actual fun DetailClientBalance(
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
    isWideScreen: Boolean,
    onBrandSelect: (String) -> Unit,
    onBranchSelect: (String) -> Unit,
    onTypeSelect: (String) -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    var expandedBrand by remember { mutableStateOf(false) }
    var expandedBranch by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterSection(
                brandSelected,
                brands,
                expandedBrand,
                { expandedBrand = it },
                onBrandSelect,
                branchSelected,
                branches,
                expandedBranch,
                { expandedBranch = it },
                onBranchSelect,
                typeSelected,
                types,
                expandedType,
                { expandedType = it },
                onTypeSelect
            )

            BalanceCard(balance, pendingAmount)

            Text(
                "Documentos en cuenta",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            DocumentList(
                documents = documents,
                invoicesWithPending = invoicesWithPending,
                wideMode = false
            ) { onInvoiceClick(it) }
        }
    }
}