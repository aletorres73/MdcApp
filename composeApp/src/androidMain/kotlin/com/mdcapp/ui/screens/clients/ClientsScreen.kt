package com.mdcapp.ui.screens.clients

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
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

    val listState = rememberLazyListState()

    BackHandler(enabled = isSearchEnable) { isSearchEnable = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Clientes MDC")
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val query = remember { mutableStateOf(TextFieldValue("")) }
            SearchbarTopBar(
                query = query.value,
                onQueryChange = { newQuery -> query.value = newQuery },
                onCleanQuery = { query.value = TextFieldValue("") },
                onClose = {
                    isSearchEnable = false
                },
                onSearch = {
                    Log.i("Search", "Search: ${query.value.text}")
                    vm.searchClients(query.value.text)
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .padding(horizontal = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(4.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
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
                        ShowClientList(state.dataSearch, state, listState) { id ->
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