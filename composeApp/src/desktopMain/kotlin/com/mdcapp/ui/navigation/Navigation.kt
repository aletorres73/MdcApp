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
import com.mdcapp.ui.screens.orders.DesktopOrdersScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    var openDetailOrderWindow by remember { mutableStateOf(false) }
    var order by remember { mutableStateOf(OrderModel()) }

    NavHost(navController = navController, startDestination = "Orders") {
        composable(route = "Orders") {
            DesktopOrdersScreen(onOpenOrderDetail = {
                openDetailOrderWindow = true
                order = it
            })
            if (openDetailOrderWindow)
                OpenDetailWindow(
                    order = order,
                    onCloseRequest = { openDetailOrderWindow = false })
        }
    }

}