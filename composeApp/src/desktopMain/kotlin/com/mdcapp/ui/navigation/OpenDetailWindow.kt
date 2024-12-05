package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.screens.orderdetail.DesktopOrderDetailScreen

@Composable
fun OpenDetailWindow(
    order: OrderModel,
    onCloseRequest: () -> Unit = {}
) {
    val windowState = rememberWindowState(
        size = DpSize(600.dp, 780.dp),
        position = WindowPosition(alignment = Alignment.CenterEnd)
    )
    Window(
        onCloseRequest = { onCloseRequest() },
        title = "Detalle de orden",
//        alwaysOnTop = true,
        state = windowState,
        resizable = false
    ) {
        DesktopOrderDetailScreen(order = order)
    }
}