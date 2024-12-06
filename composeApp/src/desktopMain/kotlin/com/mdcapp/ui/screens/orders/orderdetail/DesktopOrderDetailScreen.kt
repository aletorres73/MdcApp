package com.mdcapp.ui.screens.orders.orderdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.detailorders.OrderDetailInfo

@Composable
fun DesktopOrderDetailScreen(
    order: OrderModel,
) {
    Screen { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            OrderDetailInfo(order = order)
        }
    }
}




