package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdcapp.ui.clients.ClientsScreen
import com.mdcapp.ui.home.HomeScreen
import com.mdcapp.ui.home.orders.AndroidOrdersScreen

@Composable
fun AndroidNavigation(
    route: String,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = route
    ) {
        composable(route = "Orders") {
            AndroidOrdersScreen()
        }
        composable(route = "Home") {
            HomeScreen()
        }
        composable(route = "Clients") {
            ClientsScreen()
        }
    }

}