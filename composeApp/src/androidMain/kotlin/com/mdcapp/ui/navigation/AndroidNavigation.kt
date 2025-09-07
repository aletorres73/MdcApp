package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.ui.screens.clients.ClientsScreen
import com.mdcapp.ui.screens.detailorder.OrderDetailScreen
import com.mdcapp.ui.screens.home.HomeScreen
import com.mdcapp.ui.screens.invoices.InvoicesScreen
import com.mdcapp.ui.screens.orders.AndroidOrdersScreen
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AndroidNavigation(startRoute: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(route = AppRoute.Home.route) {
            HomeScreen { factoryName ->
                navController.navigateToOrders(factoryName)
            }
        }
        composable(route = AppRoute.Clients.route) {
            ClientsScreen { clientId -> navController.navigateToInvoices(clientId) }
        }
        composable(
            route = AppRoute.Orders.BASE_ROUTE,
            arguments = listOf(navArgument("factoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val factoryName = checkNotNull(backStackEntry.arguments?.getString("factoryName"))
            AndroidOrdersScreen(
                factoryName = factoryName,
                onDetailClick = { orderId ->
                    navController.navigateToOrderDetail(orderId, factoryName)
                },
                onBackPressed = { navController.navigate(AppRoute.Home.route) }
            )
        }

        composable(
            route = AppRoute.Invoices.BASE_ROUTE,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) {
            val clientId = it.arguments?.getString("clientId") ?: return@composable
            InvoicesScreen(clientId) {
                navController.popBackStack()
            }
        }


        composable(
            route = AppRoute.OrderDetail.BASE_ROUTE,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("factoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = checkNotNull(backStackEntry.arguments?.getString("orderId"))
            val factoryName = checkNotNull(backStackEntry.arguments?.getString("factoryName"))
            OrderDetailScreen(orderId, factoryName) {
                navController.popBackStack()
            }
        }
    }
}


fun NavHostController.navigateToOrders(factoryName: String) {
    this.navigate(AppRoute.Orders.createRoute(factoryName))
}

fun NavHostController.navigateToOrderDetail(orderId: String, factoryName: String) {
    this.navigate(AppRoute.OrderDetail.createRoute(orderId, factoryName))
}

fun NavHostController.navigateToInvoices(clientId: String) {
    this.navigate(AppRoute.Invoices.createRoute(clientId))
}