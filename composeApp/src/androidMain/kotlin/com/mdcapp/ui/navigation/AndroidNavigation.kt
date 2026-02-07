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
import com.mdcapp.ui.screens.invoicesClientDetail.DetailInvoiceScreen
import com.mdcapp.ui.screens.invoicesClientDetail.InvoicesScreen
import com.mdcapp.ui.screens.invoicesPage.InvoicesPageScreen
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
        composable(route = AppRoute.InvoicesPaged.route) {
            InvoicesPageScreen(
                onNavigation = { navController.navigate(it.route) },
                onNavigationClientDetail = { clientId -> navController.navigateToInvoices(clientId) }
            )
        }

        composable(route = AppRoute.Clients.route) {
            ClientsScreen(
                onItemClick = { clientId -> navController.navigateToInvoices(clientId) },
                onNavigation = { navController.navigate(it.route) }
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
//            val state by vm.state.collectAsState()

            DetailInvoiceScreen(
                vm = vm,
//                onSelectCondition = { brand -> vm.getPaymentCondition(brand) },
                onBack = { navController.popBackStack() })
        }
    }
}

fun NavHostController.navigateToInvoices(clientId: String) {
    this.navigate(AppRoute.Invoices.createRoute(clientId))
}

fun NavHostController.navigateToDetailInvoice(invoiceNumber: String) {
    this.navigate(AppRoute.DetailInvoice.createRoute(invoiceNumber))
}