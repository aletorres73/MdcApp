package com.mdcapp.ui.screens.invoicesClientDetail

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.formatter
import com.mdcapp.ui.composables.common.DatePicker
import com.mdcapp.ui.screens.orders.OrderCard
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailInvoiceScreen(
    vm: DetailInvoiceViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val buyOrder = state.buyOrder

    var showSheet by remember { mutableStateOf(false) }

    var showArticles by remember { mutableStateOf(false) }
    var showOrder by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
//    val datePickerState = rememberDatePickerState()

    BackHandler { onBack() }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            PaymentConditionListSheet(
                list = state.paymentConditionList,
                onSelect = { condition ->
                    vm.updateSelectedPaymentCondition(condition)
                    showSheet = false
                }
            )
        }
    }

    if (showDatePicker) {
        /*DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = updatedReceptionDate(datePickerState, vm)
                }) {
                    Text("Aceptar")
                }
            }
        ) {
            DatePicker(
                onDismissRequest = { showDatePicker = false },
                onConfirmButton = {
                    showDatePicker = updatedReceptionDate(datePickerState, vm)
                },
                onDismissButton = {showDatePicker = false},
                enable = true,
            )
        }*/
        DatePicker(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { onDateSelected ->
                vm.updateDeliveryDate(onDateSelected.formatter())
                showDatePicker = false
//                showDatePicker = updatedReceptionDate(datePickerState, vm)
            },
            onDismissButton = { showDatePicker = false },
            enable = true
        )
    }

    LaunchedEffect(state.message) {
        state.message.let {
            scope.launch {
                if (it != null) {
                    snackBarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                    vm.clearMessage()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            InvoiceHeaderTopBar(
                billing = state.billing,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            DatesCard(billing = state.billing) { showDatePicker = true }

            TotalsCard(billing = state.billing)

            ArticlesCard(
                articles = state.billing.articles,
                expanded = showArticles,
                onToggle = { showArticles = !showArticles }
            )

            PaymentConditionCard(billing = state.billing) { showSheet = true }

            OrderCard(
                order = buyOrder,
                expanded = showOrder,
                onToggle = { showOrder = !showOrder }
            )
        }
    }
}




