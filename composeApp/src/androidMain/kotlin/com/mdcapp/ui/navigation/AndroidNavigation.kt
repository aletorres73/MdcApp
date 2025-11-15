package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.mdcapp.ui.screens.invoices.DetailInvoiceScreen
import com.mdcapp.ui.screens.invoices.InvoicesScreen
import com.mdcapp.ui.screens.orders.AndroidOrdersScreen
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

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
            val vm: InvoicesViewModel = koinViewModel(parameters = { parametersOf(clientId) })
            InvoicesScreen(
                vm,
                onBack = { navController.popBackStack() },
                onNavigate = { navController.popBackStack() },
                onInvoiceClick = { invoiceNumber ->
                    navController.navigateToDetailInvoice(
                        invoiceNumber
                    )
                }
            )
        }

        composable(
            route = AppRoute.DetailInvoice.BASE_ROUTE,
            arguments = listOf(navArgument("invoiceNumber") { type = NavType.StringType })
        ) {
            val invoiceNumber = it.arguments?.getString("invoiceNumber") ?: return@composable
            val vm: DetailInvoiceViewModel =
                koinViewModel(parameters = { parametersOf(invoiceNumber) })
            val state by vm.state.collectAsState()
            DetailInvoiceScreen(state.billing)
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

fun NavHostController.navigateToDetailInvoice(invoiceNumber: String) {
    this.navigate(AppRoute.DetailInvoice.createRoute(invoiceNumber))
}