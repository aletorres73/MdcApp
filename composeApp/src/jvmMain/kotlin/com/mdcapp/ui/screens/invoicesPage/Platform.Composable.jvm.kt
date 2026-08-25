package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        // Layout de 2 columnas para Desktop
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Columna Izquierda: Filtros y Saldo
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Opciones de Filtro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            onTypeSelect,
                            isVertical = true
                        )
                    }
                }

                BalanceCard(balance, pendingAmount, isWideScreen = true)
            }

            // Columna Derecha: Documentos
            Column(
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 24.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Documentos en cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                DocumentList(
                    documents = documents,
                    invoicesWithPending = invoicesWithPending,
                    wideMode = true
                ) { onInvoiceClick(it) }
            }
        }
    }
}