package com.mdcapp.ui.screens.clients

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.composables.common.SearchbarTopBar
import com.mdcapp.ui.viewmodels.ClientsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ClientsScreen(
    vm: ClientsViewModel = koinViewModel(),
    onItemClick: (id: String) -> Unit
) {
    val state by vm.state.collectAsState()
    val statusScreenStatus by vm.statusScreen.collectAsState()

    var isSearchEnable by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    BackHandler(enabled = isSearchEnable) {
        isSearchEnable = false
        /*Todo limpiar la búsqueda de clientes*/
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= state.data.lastIndex - 4 &&
                    !state.updatingData &&
                    state.hasMore
                ) {
                    vm.loadNextPage()
                }
            }
    }


    Scaffold(
        topBar = {
            AnimatedVisibility(!isSearchEnable) {
                TopAppBar(
                    title = {
                        Text(text = "Clientes MDC")
                    },
                    actions = {
                        IconButton(onClick = { isSearchEnable = !isSearchEnable }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = isSearchEnable,
            ) {
                val query = remember { mutableStateOf(TextFieldValue("")) }
                SearchbarTopBar(
                    query = query.value,
                    onQueryChange = { newQuery -> query.value = newQuery },
                    onCleanQuery = { query.value = TextFieldValue("") },
                    onClose = {
                        isSearchEnable = false
                        vm.resetView()
                    },
                    onSearch = {
                        Log.i("Search", "Search: ${query.value.text}")
                        vm.searchClients(query.value.text)
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ID",
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Razón Social",
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Clientes ${state.amountClients}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            when (statusScreenStatus) {
                is ClientsViewModel.ClientScreenStatus.Idle -> {
                    val message =
                        (statusScreenStatus as ClientsViewModel.ClientScreenStatus.Idle).message
                    Column {
                        ShowClientList(state.data, state, listState) { id ->
                            onItemClick(id)
                            Log.i("Client", "Client: $id")
                        }
                    }
                    LaunchedEffect(message) {
                        if (message.isNotEmpty())
                            snackBarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short
                            )
                    }
                }

                ClientsViewModel.ClientScreenStatus.Search -> {
                    ShowClientList(state.dataSearch, state, listState) { id ->
                        onItemClick(id)
                        Log.i("Client", "Client: $id")
                    }
                }
            }
        }
    }
}