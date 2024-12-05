package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.screens.orderdetail.DesktopOrderDetailScreen

@Composable
fun OpenDetailWindow(
    order: OrderModel,
    onCloseRequest: () -> Unit = {}
) {
    Window(
        onCloseRequest = { onCloseRequest() },
        title = "Nueva Ventana",
        alwaysOnTop = true
    ) {
        DesktopOrderDetailScreen(order = order)
    }
}