package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.ui.composables.common.LoadingOverlay
import com.mdcapp.ui.composables.common.RefreshContainer
import com.mdcapp.ui.composables.common.SearchBar
import com.mdcapp.ui.utils.AppBackHandler
import com.mdcapp.ui.utils.getAppInstaller
import com.mdcapp.ui.viewmodels.invoices.InvoicesPagedViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun InvoicesPageScreen(
    viewModel: InvoicesPagedViewModel = koinViewModel(),
    onNavigationInvoice: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Bloqueo de botón atrás si hay actualización forzada
    if (state.updateState == UpdateState.FORCE_UPDATE) {
        AppBackHandler(enabled = true) {
            // Bloqueado
        }
    }

    if (state.isSearchMode) {
        AppBackHandler {
            viewModel.setSearchMode(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.isSearchMode) {
            SearchBar(
                query = state.searchQuery.text,
                onQueryChange = { viewModel.onQueryChange(it) },
                onCleanQuery = { viewModel.clearQuery() },
                onBack = { viewModel.setSearchMode(false) },
                searchText = "Buscar cliente o número..."
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setSearchMode(true) }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }

                StateFilter(
                    states = state.availableStates,
                    selected = state.selectedState,
                    onSelected = {
                        viewModel.stateSelected(it)
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.displayInvoices.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                }

                RefreshContainer(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.weight(1f)
                ) {
                    InvoiceList(
                        invoices = state.displayInvoices,
                        isLoading = state.isLoading,
                        onLoadMore = { viewModel.loadNextPage() },
                        onNavigationInvoice = onNavigationInvoice,
                        invoicesWithPending = state.invoicesWithPending
                    )
                }
            }

            // Verdadero Overlay sin desplazamientos
            if (state.displayInvoices.isEmpty() && state.isLoading) {
                LoadingOverlay(true)
            }
        }
    }

    // ----- overlays -----

    when (val overlay = state.overlay) {
        InvoicesPagedViewModel.Overlay.None -> {}
        is InvoicesPagedViewModel.Overlay.UpdateApp -> {
            when (val updateState = overlay.state) {
                UpdateState.OK -> {}
                UpdateState.OPTIONAL_UPDATE, UpdateState.FORCE_UPDATE -> {
                    val installer = getAppInstaller()
                    UpdateDialog(
                        type = updateState,
                        releaseNotes = overlay.releasesNotes,
                        onUpdate = { viewModel.updateApk(installer) },
                        onDismiss = { viewModel.closeOverlay() }
                    )
                }
            }
        }
    }
}

