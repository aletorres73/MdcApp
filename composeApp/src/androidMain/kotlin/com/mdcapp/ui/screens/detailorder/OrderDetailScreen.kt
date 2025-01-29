package com.mdcapp.ui.screens.detailorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.detailorders.OrderDetailInfo
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    factoryName: String,
    vm: BuyOrdersViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isDataChanged by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Screen {
        ShowDiscardChangesDialog(
            enable = isDataChanged,
            onDiscard = {
                isDataChanged = false
                onBackPressed()
            }
        ) {
            println("data saved")
            vm.saveData()
            scope.launch {
                if (vm.state.result)
                    snackBarHostState.showSnackbar(
                        message = "Datos guardados",
                        duration = SnackbarDuration.Short
                    )
                else
                    snackBarHostState.showSnackbar(
                        message = "Error",
                        duration = SnackbarDuration.Short
                    )
            }
            onBackPressed()
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                MainTopAppBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onPrimaryContainer),
                    titleContent = {
                        Text(
                            "Orden \t$orderId",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    isNavigationOn = true,
                    onBack = {
                        if (vm.dataChanged()) isDataChanged = true
                        else {
                            println("Don't data saved")
                            onBackPressed()
                        }
                    },
                    onActions = {
                        IconButton(onClick = {
                            if (vm.dataChanged()) {
                                println("data saved")
                                vm.saveData()
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Datos guardados",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else println("Don't data saved")
                        }) { Icon(imageVector = Icons.TwoTone.Save, contentDescription = null) }
                    }
                )
            },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.Start
            ) {
                HorizontalDivider()
                OrderDetailInfo(
                    orderId = orderId,
                    onBillingClicked = {},
                    factoryName = factoryName,
                    vm = vm
                )
            }
        }
    }
}

@Composable
fun ShowDiscardChangesDialog(enable: Boolean, onDiscard: () -> Unit, onSave: () -> Unit) {
    if (enable) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Descartar cambios") },
            text = { Text("¿Deseas descartar los cambios realizados?") },
            confirmButton = {
                TextButton(onClick = onDiscard) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = onSave) {
                    Text("Guardar")
                }
            }
        )
    }
}
