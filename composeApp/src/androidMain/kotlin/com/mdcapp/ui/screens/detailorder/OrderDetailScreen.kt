package com.mdcapp.ui.screens.detailorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Star
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    // Estado para controlar si los datos han cambiado
    var isDataChanged by remember { mutableStateOf(false) }

    // Snackbar para mostrar mensajes al usuario
    val snackBarHostState = remember { SnackbarHostState() }

    // CoroutineScope para lanzar corrutinas
    val scope = rememberCoroutineScope()

    // Inicializar el ViewModel con el orderId y factoryName
    LaunchedEffect(orderId, factoryName) {
        vm.init(orderId, factoryName)
    }

    // Observar cambios en el estado del ViewModel
    val state by vm.state.collectAsState()

    // Mostrar diálogo de descartar cambios si los datos han sido modificados
    ShowDiscardChangesDialog(
        enable = isDataChanged,
        onDiscard = {
            isDataChanged = false
            onBackPressed()
        },
        onSave = {
            scope.launch {
                vm.saveData()
                val message = if (state.result) "Datos guardados" else "Error al guardar"
                snackBarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                if (state.result) onBackPressed()
            }
        }
    )

    // Comportamiento de scroll para la TopAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Estructura principal de la pantalla
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            MainTopAppBar(
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onPrimaryContainer),
                /*   titleContent = {
                       Text(
                           "Orden \t$orderId",
                           style = MaterialTheme.typography.titleMedium
                       )
                   }*/
                title = "Orden 	$orderId",
                isNavigationOn = true,
                onBack = {
                    if (vm.dataChanged()) isDataChanged = true
                    else onBackPressed()
                },
                onActions = {
                    IconButton(
                        onClick = {
                            if (vm.dataChanged()) {
                                scope.launch {
                                    vm.saveData()
                                    val message =
                                        if (state.result) "Datos guardados" else "Error al guardar"
                                    snackBarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.TwoTone.Star, contentDescription = null)
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        // Envolver el contenido en un Column con scroll vertical
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Habilita el scroll vertical
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
@Composable
private fun ShowDiscardChangesDialog(
    enable: Boolean,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    if (enable) {
        AlertDialog(
            onDismissRequest = { /* No se permite cerrar sin tomar una decisión */ },
            title = { Text("¿Guardar cambios?") },
            text = { Text("Tienes cambios sin guardar. ¿Deseas guardarlos antes de salir?") },
            confirmButton = {
                TextButton(onClick = onSave) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscard) {
                    Text("Descartar")
                }
            }
        )
    }
}
