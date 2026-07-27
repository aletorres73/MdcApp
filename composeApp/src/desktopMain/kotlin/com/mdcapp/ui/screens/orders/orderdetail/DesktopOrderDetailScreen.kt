package com.mdcapp.ui.screens.orders.orderdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.OrderModel
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.detailorders.OrderDetailInfo
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DesktopOrderDetailScreen(
    order: OrderModel,
    vm: BuyOrdersViewModel = koinViewModel(),
    factoryName: String,
    onBillingClicked: (BillingModel) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.init(orderId = order.orderNumber, factoryName = factoryName)
    }
    Screen {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                OrderDetailInfo(
                    orderId = order.orderNumber,
                    onBillingClicked = { billing -> onBillingClicked(billing) },
                    factoryName = factoryName,
                    vm = vm
                )
            }
        }
    }
}





