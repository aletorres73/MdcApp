package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    vm: InvoicesViewModel,
    onNavigate: () -> Unit,
    onBack: () -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    BackHandler { onBack() }

    val state by vm.state.collectAsState()
    val brandSelected by vm.selectedBrand.collectAsState()
    val branchSelected by vm.selectedBranch.collectAsState()
    val typeSelected by vm.selectedType.collectAsState()

    val brands = state.brandList
    val branches = state.branchList
    val types = state.typeList
    /*    LaunchedEffect(brands) {
            if (brands.isNotEmpty()) brandSelected = brands.first()
        }*/

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
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                )
                { LoadingIndicator(true) }
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
                DetailClientBalance(
                    paddingValues = paddingValues,
                    documents = state.documents,
                    balance = state.balance,
                    brandSelected = brandSelected,
                    branchSelected = branchSelected,
                    typeSelected = typeSelected,
                    brands = brands,
                    branches = branches,
                    types = types,
                    onBrandSelect = { vm.setBrand(it) },
                    onBranchSelect = { vm.setBranch(it) },
                    onTypeSelect = { vm.setType(it) }
                ) {
                    onInvoiceClick(it)
                }
            }
        }

    }
}

@Composable
fun DetailClientBalance(
    paddingValues: PaddingValues,
    documents: List<BillingModel>,
    balance: Double,
    brandSelected: String,
    branchSelected: String,
    typeSelected: String,
    brands: List<String>,
    branches: List<String>,
    types: List<String>,
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
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section: Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter: Factory
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fábrica",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        AssistChip(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { expandedBrand = true },
                            label = {
                                Text(
                                    text = brandSelected.ifEmpty { "Todas" },
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expandedBrand,
                            onDismissRequest = { expandedBrand = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        onBrandSelect(brand)
                                        expandedBrand = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter: Segment
                val isBranchEnabled = brandSelected != "Todas" && brandSelected.isNotEmpty()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Segmento",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isBranchEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        AssistChip(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isBranchEnabled,
                            onClick = { expandedBranch = true },
                            label = {
                                Text(
                                    text = if (isBranchEnabled) branchSelected.ifEmpty { "Todas" } else "---",
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expandedBranch,
                            onDismissRequest = { expandedBranch = false }
                        ) {
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch) },
                                    onClick = {
                                        onBranchSelect(branch)
                                        expandedBranch = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter: Type (Factura/Remito)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tipo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        AssistChip(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { expandedType = true },
                            label = {
                                Text(
                                    text = typeSelected.ifEmpty { "Todos" },
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        onTypeSelect(type)
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            androidx.compose.material3.ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Saldo total de cuenta",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$$balance",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                "Documentos en cuenta",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            DocumentList(documents) { onInvoiceClick(it) }
        }
    }
}




