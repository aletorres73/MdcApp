package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mdcapp.ui.screens.clients.ClientsScreen
import com.mdcapp.ui.screens.home.HomeScreen
import com.mdcapp.ui.screens.orders.AndroidOrdersScreen

@Composable
fun AndroidNavigation(
    route: String,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = route
    ) {
        composable(
            route = "Orders/{factoryName}",
            arguments = listOf(navArgument("factoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val factoryName = checkNotNull(backStackEntry.arguments?.getString("factoryName"))
            AndroidOrdersScreen(factoryName = factoryName)
        }
        composable(route = "Home") {
            HomeScreen { factoryName ->
                navController.navigate("Orders/$factoryName")
            }
        }
        composable(route = "Clients") {
            ClientsScreen()
        }
    }

}