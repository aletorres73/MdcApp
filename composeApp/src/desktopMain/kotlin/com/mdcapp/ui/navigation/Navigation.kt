package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.composables.billings.BillingItem
import com.mdcapp.ui.screens.common.OpenWindow
import com.mdcapp.ui.screens.orders.DesktopOrdersScreen
import com.mdcapp.ui.screens.orders.orderdetail.DesktopOrderDetailScreen


@Composable
fun Navigation() {
    val navController = rememberNavController()
    var openDetailOrderWindow by remember { mutableStateOf(false) }
    var openDetailBillingWindow by remember { mutableStateOf(false) }
    var order by remember { mutableStateOf(OrderModel()) }
    var billing by remember { mutableStateOf(BillingModel()) }

    NavHost(navController = navController, startDestination = "Orders") {
        composable(route = "Orders") {
            DesktopOrdersScreen(onOpenOrderDetail = {
                openDetailOrderWindow = true
                order = it
            })
            if (openDetailOrderWindow)
                OpenWindow(
                    onCloseRequest = { openDetailOrderWindow = false },
                    title = "Detalle de orden: ${order.orderNumber}",
                    content = {
                        DesktopOrderDetailScreen(
                            order = order,
                            onBillingClicked = {
                                billing = it
                                openDetailBillingWindow = true
                            }
                        )
                    }
                )
            if (openDetailBillingWindow) {
                OpenWindow(
                    onCloseRequest = { openDetailBillingWindow = false },
                    title = "Detalle de facturación : ${billing.billingNumber}",
                    content = {
                        BillingItem(billing = billing)
                    },
                    size = DpSize(600.dp, 390.dp),
                    onTop = true
                )
            }
        }
    }
}