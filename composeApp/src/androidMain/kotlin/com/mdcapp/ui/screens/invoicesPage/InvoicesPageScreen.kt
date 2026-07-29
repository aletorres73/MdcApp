package com.mdcapp.ui.screens.invoicesPage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.ui.composables.common.SearchBar
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedContent(
            targetState = state.isSearchMode,
            transitionSpec = {
                if (targetState) {
                    // Si entramos a búsqueda: desliza desde la derecha y aparece (fade)
                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    // Si salimos de búsqueda: desliza desde la izquierda y aparece (fade)
                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "SearchTransition"
        ) { isSearching ->
            if (isSearching) {
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
                        counts = state.stateCounts,
                        onSelected = {
                            viewModel.stateSelected(it)
                        }
                    )
                }
            }
        }

        InvoiceList(
            invoices = state.displayInvoices,
            isLoading = state.isLoading,
            onLoadMore = { /* No longer needed with full memory sync */ },
            onNavigationInvoice = onNavigationInvoice,
            invoicesWithPending = state.invoicesWithPending
        )
// ...

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

