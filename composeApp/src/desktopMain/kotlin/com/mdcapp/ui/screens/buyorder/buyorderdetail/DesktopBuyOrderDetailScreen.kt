package com.mdcapp.ui.screens.buyorder.buyorderdetail

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

@Composable
fun DesktopBuyOrderDetailScreen(
    order: OrderModel
) {
    Screen { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
        }
    }
}




