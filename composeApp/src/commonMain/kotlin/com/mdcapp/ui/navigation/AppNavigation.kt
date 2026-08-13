package com.mdcapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.ui.screens.AddClientScreen
import com.mdcapp.ui.screens.AddInvoiceScreen
import com.mdcapp.ui.screens.ClientOrdersScreen
import com.mdcapp.ui.screens.CreateOrderScreen
import com.mdcapp.ui.screens.FactoryManagementScreen
import com.mdcapp.ui.screens.LoginScreen
import com.mdcapp.ui.screens.MainScreen
import com.mdcapp.ui.screens.OrderDetailScreen
import com.mdcapp.ui.screens.PaymentHistoryScreen
import com.mdcapp.ui.screens.ProfileScreen
import com.mdcapp.ui.screens.SignUpScreen
import com.mdcapp.ui.screens.SubscriptionStatusScreen
import com.mdcapp.ui.screens.invoicesClientDetail.DetailInvoiceScreen
import com.mdcapp.ui.screens.invoicesClientDetail.InvoicesScreen
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavigation(startRoute: String) {
    val navController = rememberNavController()
    val authRepo: com.mdcapp.domain.repositories.AuthRepository = koinInject()
    val userService: com.mdcapp.data.service.UserService = koinInject()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Lógica de redirección por suscripción en tiempo real
    androidx.compose.runtime.LaunchedEffect(Unit) {
        userService.observeUserProfile().collectLatest { profile ->
            val currentRoute = navController.currentDestination?.route
            if (authRepo.isLogged() &&
                currentRoute != AppRoute.Login.route &&
                currentRoute != AppRoute.SignUp.route &&
                currentRoute != AppRoute.SubscriptionStatus.route
            ) {
                val isExpired = (profile?.subscriptionExpiresAt ?: 0) < System.currentTimeMillis()
                val isManuallyEnabled = profile?.isManuallyEnabled == true

                if (isExpired && !isManuallyEnabled) {
                    navController.navigate(AppRoute.SubscriptionStatus.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            } else if (authRepo.isLogged() && currentRoute == AppRoute.SubscriptionStatus.route) {
                // Si estamos en la pantalla de bloqueo y la suscripción se activa, salir de ahí
                val isExpired = (profile?.subscriptionExpiresAt ?: 0) < System.currentTimeMillis()
                val isManuallyEnabled = profile?.isManuallyEnabled == true
                if (!isExpired || isManuallyEnabled) {
                    navController.navigate(AppRoute.InvoicesPaged.route) {
                        popUpTo(AppRoute.SubscriptionStatus.route) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(route = AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.InvoicesPaged.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppRoute.SignUp.route)
                }
            )
        }

        composable(route = AppRoute.SignUp.route) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(AppRoute.InvoicesPaged.route) {
                        popUpTo(AppRoute.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoute.AddClient.BASE_ROUTE,
            arguments = listOf(
                navArgument("id") { nullable = true; defaultValue = null },
                navArgument("name") { nullable = true; defaultValue = null }
            )
        ) {
            val id = it.arguments?.getString("id")
            val name = it.arguments?.getString("name")
            AddClientScreen(
                initialId = id,
                initialName = name,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.CreateOrder.BASE_ROUTE,
            arguments = listOf(
                navArgument("clientId") { nullable = true; defaultValue = null },
                navArgument("orderId") { nullable = true; defaultValue = null }
            )
        ) {
            val clientId = it.arguments?.getString("clientId")
            val orderId = it.arguments?.getString("orderId")
            CreateOrderScreen(
                clientId = clientId,
                orderId = orderId,
                onBack = { navController.popBackStack() },
                onManageFactories = { navController.navigate(AppRoute.Factories.route) }
            )
        }

        composable(route = AppRoute.Factories.route) {
            FactoryManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.AddInvoice.BASE_ROUTE,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
                navArgument("orderId") { type = NavType.StringType }
            )
        ) {
            val clientId = it.arguments?.getString("clientId") ?: return@composable
            val orderId = it.arguments?.getString("orderId") ?: return@composable
            AddInvoiceScreen(
                clientId = clientId,
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.OrderDetail.BASE_ROUTE,
            arguments = listOf(
                navArgument("clientId") { nullable = true; defaultValue = null },
                navArgument("orderId") { nullable = true; defaultValue = null },
                navArgument("factoryName") { nullable = true; defaultValue = null }
            )
        ) {
            val clientId = it.arguments?.getString("clientId") ?: return@composable
            val orderId = it.arguments?.getString("orderId") ?: return@composable
            val factoryName = it.arguments?.getString("factoryName") ?: return@composable
            OrderDetailScreen(
                clientId = clientId,
                orderId = orderId,
                factoryName = factoryName,
                onBack = { navController.popBackStack() },
                onNavigateToInvoice = { invoiceNumber ->
                    navController.navigateToDetailInvoice(invoiceNumber)
                },
                onEditOrder = { cId, oId ->
                    navController.navigate(AppRoute.CreateOrder.createRoute(cId, oId))
                }
            )
        }

        composable(route = AppRoute.InvoicesPaged.route) {
            MainScreen(
                onNavigateToInvoice = { invoiceNumber ->
                    navController.navigateToDetailInvoice(invoiceNumber)
                },
                onNavigateToClientOrders = { clientId ->
                    navController.navigate(AppRoute.ClientOrders.createRoute(clientId))
                },
                onNavigateToCurrentAccount = { clientId ->
                    navController.navigateToInvoices(clientId)
                },
                onNavigateToAddClient = {
                    navController.navigate(AppRoute.AddClient.createRoute())
                },
                onNavigateToEditClient = { id, name ->
                    navController.navigate(AppRoute.AddClient.createRoute(id, name))
                },
                onNavigateToCreateOrder = {
                    navController.navigate(AppRoute.CreateOrder.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                },
                onNavigateToPaymentHistory = {
                    navController.navigate(AppRoute.PaymentHistory.route)
                },
                onManageFactories = {
                    navController.navigate(AppRoute.Factories.route)
                },
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.InvoicesPaged.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = AppRoute.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoute.PaymentHistory.route) {
            PaymentHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoute.SubscriptionStatus.route) {
            SubscriptionStatusScreen(
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = AppRoute.ClientOrders.BASE_ROUTE,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) {
            val clientId = it.arguments?.getString("clientId") ?: return@composable
            ClientOrdersScreen(
                clientId = clientId,
                onBack = { navController.popBackStack() },
                onAddOrder = { id ->
                    navController.navigate(AppRoute.CreateOrder.createRoute(id))
                },
                onOrderClick = { orderId, factoryName ->
                    navController.navigate(
                        AppRoute.OrderDetail.createRoute(
                            clientId,
                            orderId,
                            factoryName
                        )
                    )
                },
                onAssignInvoice = { orderId ->
                    navController.navigate(AppRoute.AddInvoice.createRoute(clientId, orderId))
                }
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

            DetailInvoiceScreen(
                vm = vm,
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
