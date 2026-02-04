package com.mdcapp.ui.composables.invoicesPage

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.ui.composables.common.SearchBar
import com.mdcapp.ui.viewmodels.invoices.InvoicesPagedViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun InvoicesPageScreen(
    viewModel: InvoicesPagedViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val query = remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = { Text("Facturas") },
                actions = {
                }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InputSearchBar { onType -> viewModel.onSelectedTypeSearch(onType) }
                StateFilter(
                    states = state.availableStates,
                    selected = state.selectedState,
                    onSelected = {
                        viewModel.stateSelected(it)
                    }
                )
            }
            SearchBar(
                query = query.value,
                onQueryChange = { newQuery -> query.value = newQuery },
                onCleanQuery = {
                    query.value = TextFieldValue("")
                    viewModel.clearQuery()
                },
                onSearch = {
                    Log.i("Search", "Search: ${query.value.text}")
                    viewModel.onSearch(query.value.text)
                },
                searchText = "Selecionar documento / cliente..."
            )
            InvoiceList(
                invoices = state.invoices,
                isLoading = state.isLoading,
                onLoadMore = { viewModel.loadNextPage() }
            )
        }
    }

}

@Composable
fun InputSearchBar(onTypeSelected: (TypeSearch) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var selectedInput by remember { mutableStateOf(false) }
        InputChip(
            selected = !selectedInput,
            onClick = {
                onTypeSelected(TypeSearch.Client)
                selectedInput = !selectedInput
            },
            label = { Text("Cliente") }
        )
        InputChip(
            selected = selectedInput,
            onClick = {
                onTypeSelected(TypeSearch.Number)
                selectedInput = !selectedInput
            },
            label = { Text("Numero") }
        )
    }
}

sealed class TypeSearch {
    data object Client : TypeSearch()
    data object Number : TypeSearch()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateFilter(
    states: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.CenterEnd
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            InputChip(
                selected = false,
                onClick = { expanded = true },
                label = { Text(selected) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 160.dp)
            ) {
                states.forEach { state ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = state,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(state)
                        }
                    )
                }
            }
        }
    }
}


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

@Composable
fun InvoiceRow(invoice: BillingModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Fecha: ${invoice.loadDate}", style = MaterialTheme.typography.titleSmall)
            Text(invoice.clientName, style = MaterialTheme.typography.titleSmall)
            Text("Total: $${invoice.total}")
            Text("Estado: ${invoice.stateBilling}")
        }
    }
}


