package com.mdcapp.ui.screens.invoicesPage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.TypeSearch
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.ui.composables.common.SearchBar
import com.mdcapp.ui.viewmodels.invoices.InvoicesPagedViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun InvoicesPageScreen(
    viewModel: InvoicesPagedViewModel = koinViewModel(),
    onNavigationInvoice: (String) -> Unit = {},
    onNavigationClientDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val query = state.searchQuery

    // Forzar recarga al entrar
    LaunchedEffect(Unit) {
        viewModel.loadNextPage()
    }

    val suggestions = remember(
        query,
        state.clientNameList,
        state.typeSearch
    ) {
        if (
            state.typeSearch != TypeSearch.Client ||
            query.text.isBlank()
        ) {
            emptyList()
        } else {
            state.clientNameList
                .filter {
                    it.clientName.startsWith(query.text, ignoreCase = true)
                }
                .take(10) // límite razonable
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
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
            query = query.text,
            onQueryChange = { viewModel.onQueryChange(it) },
            onCleanQuery = { viewModel.clearQuery() },
            onSearch = { viewModel.onSearch() },
            searchText = "Seleccionar cliente o documento..."
        )

        SuggestionNameCard(
            suggestions = suggestions,
            onNavigationClientDetail = { onNavigationClientDetail(it) },
            onSelect = { viewModel.selectSuggestion(it) }
        )

        InvoiceList(
            invoices = state.invoices,
            isLoading = state.isLoading,
            onLoadMore = { viewModel.loadNextPage() },
            onNavigationInvoice = onNavigationInvoice
        )

        // ----- overlays -----

        when (val overlay = state.overlay) {
            InvoicesPagedViewModel.Overlay.None -> {}
            is InvoicesPagedViewModel.Overlay.UpdateApp -> {
                when (val updateState = overlay.state) {
                    UpdateState.OK -> {}
                    UpdateState.OPTIONAL_UPDATE -> {
                        val context = LocalContext.current
                        UpdateDialog(
                            type = updateState,
                            releaseNotes = overlay.releasesNotes,
                            onUpdate = { viewModel.updateApk(context) },
                            onDismiss = { viewModel.closeOverlay() }
                        )
                    }

                    UpdateState.FORCE_UPDATE -> {
                        val context = LocalContext.current
                        UpdateDialog(
                            type = updateState,
                            releaseNotes = overlay.releasesNotes,
                            onUpdate = { viewModel.updateApk(context) },
                            onDismiss = { viewModel.closeOverlay() }
                        )
                    }
                }
            }
        }
    }
}
