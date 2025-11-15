package com.mdcapp.ui.screens.invoices

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
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

    var expanded by remember { mutableStateOf(false) }
    var brandSelected by remember { mutableStateOf(String()) }

    val brands = state.brandList
    LaunchedEffect(brands) {
        if (brands.isNotEmpty()) brandSelected = brands.first()
    }

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
                    Box {
                        AssistChip(
                            onClick = { expanded = true },
                            label = { Text(text = brandSelected) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Marcas"
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        brandSelected = brand
                                        expanded = false
                                        vm.filterByMarca(brandSelected)
                                    }
                                )
                            }
                        }
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
                DetailClientBalance(paddingValues, state.documents) { onInvoiceClick(it) }
            }
        }

    }
}

@Composable
fun DetailClientBalance(
    paddingValues: PaddingValues,
    documents: List<BillingModel>,
    onInvoiceClick: (String) -> Unit
) {
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
            Text("Documentos en cuenta", style = MaterialTheme.typography.titleMedium)

            DocumentList(documents) { onInvoiceClick(it) }
        }
    }
}



