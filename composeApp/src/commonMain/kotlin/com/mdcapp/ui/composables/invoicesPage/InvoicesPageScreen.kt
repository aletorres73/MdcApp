package com.mdcapp.ui.composables.invoicesPage

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.TypeSearch
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

    val suggestions = remember(
        query.value.text,
        state.clientNameList,
        state.typeSearch
    ) {
        if (
            state.typeSearch != TypeSearch.Client ||
            query.value.text.isBlank()
        ) {
            emptyList()
        } else {
            state.clientNameList
                .filter {
                    it.startsWith(query.value.text, ignoreCase = true)
                }
                .take(5) // límite razonable
        }
    }


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
                searchText = "Selecionar cliente o documento..."
            )

            SuggestionNameCard(suggestions, query, viewModel)

            InvoiceList(
                invoices = state.invoices,
                isLoading = state.isLoading,
                onLoadMore = { viewModel.loadNextPage() }
            )
        }
    }

}


