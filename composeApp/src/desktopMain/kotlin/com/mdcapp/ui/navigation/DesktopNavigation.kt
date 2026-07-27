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
import com.mdcapp.data.remote.RemoteResultBillingModel
import com.mdcapp.data.remote.toDomain
import com.mdcapp.domain.entities.OrderModel
import com.mdcapp.ui.composables.billings.BillingItem
import com.mdcapp.ui.screens.common.OpenWindow
import com.mdcapp.ui.screens.orders.orderdetail.DesktopOrderDetailScreen
import com.mdcapp.ui.screens.orders.orders.DesktopOrdersScreen


@Composable
fun DesktopNavigation() {
    val navController = rememberNavController()
    var openDetailOrderWindow by remember { mutableStateOf(false) }
    var openDetailBillingWindow by remember { mutableStateOf(false) }
    var orderModel by remember { mutableStateOf(OrderModel()) }
    var factoryName by remember { mutableStateOf("") }
    var billing by remember { mutableStateOf(RemoteResultBillingModel().toDomain()) }

    NavHost(navController = navController, startDestination = "Orders") {
        composable(route = "Orders") {
            DesktopOrdersScreen(
                onOpenOrderDetail = { order, branch ->
                    openDetailOrderWindow = true
                    orderModel = order
                    factoryName = if (branch == "Gummi") "IBA" else factoryName
                }
            )
            if (openDetailOrderWindow)
                OpenWindow(
                    onCloseRequest = { openDetailOrderWindow = false },
                    title = "Detalle de orden: ${orderModel.orderNumber}",
                    content = {
                        DesktopOrderDetailScreen(
                            order = orderModel,
                            factoryName = factoryName,
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
