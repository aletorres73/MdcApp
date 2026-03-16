package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.PaymentCondition
import com.mdcapp.ui.screens.orders.OrderCard
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel

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
    var selectedCondition by remember { mutableStateOf<PaymentCondition?>(null) }

    var showArticles by remember { mutableStateOf(false) }
    var showOrder by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    BackHandler { onBack() }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            PaymentConditionListSheet(
                list = state.paymentConditionList,
                onSelect = { condition ->
                    selectedCondition = condition
                    vm.updateSelectedPaymentCondition(condition) // la implementás vos
                    showSheet = false
                }
            )
        }
    }

    Scaffold(
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

            DatesCard(billing = state.billing)

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




