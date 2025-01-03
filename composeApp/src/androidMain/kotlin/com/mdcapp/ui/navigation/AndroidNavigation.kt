package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdcapp.ui.home.AndroidOrdersScreen

@Composable
fun AndroidNavigation() {
    val navController = rememberNavController()
    /*    var openDetailOrderWindow by remember { mutableStateOf(false) }
        var openDetailBillingWindow by remember { mutableStateOf(false) }
        var order by remember { mutableStateOf(OrderModel()) }
        var billing by remember { mutableStateOf(BillingModel()) }*/

    NavHost(navController = navController, startDestination = "Orders") {
        composable(route = "Orders") {
            AndroidOrdersScreen()
        }
    }

}