package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdcapp.data.model.OrderModel
import com.mdcapp.domain.usescases.handlerusescases.HandlersUsesCases
import com.mdcapp.ui.screens.common.OpenWindow
import com.mdcapp.ui.screens.orders.DesktopOrdersScreen
import com.mdcapp.ui.screens.orders.orderdetail.DesktopOrderDetailScreen
import org.koin.java.KoinJavaComponent.inject

@Composable
fun Navigation() {
    val handler: HandlersUsesCases by inject(HandlersUsesCases::class.java)
    val navController = rememberNavController()
    var openDetailOrderWindow by remember { mutableStateOf(false) }
    var openWindowHandler by remember { mutableStateOf(false) }
    var order by remember { mutableStateOf(OrderModel()) }

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
                            /*
                                                        onClick = { key, value ->
                                                            openWindowHandler = handler.loadValues(key, value)
                                                        }*/
                        )
                    }
                )
        }
    }
}