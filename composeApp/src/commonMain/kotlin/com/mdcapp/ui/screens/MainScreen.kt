package com.mdcapp.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.domain.service.RefreshController
import com.mdcapp.ui.screens.invoicesClientDetail.DetailInvoiceScreen
import com.mdcapp.ui.screens.invoicesClientDetail.InvoicesScreen
import com.mdcapp.ui.screens.invoicesPage.InvoicesPageScreen
import com.mdcapp.ui.utils.AppBackHandler
import com.mdcapp.ui.utils.closeApp
import com.mdcapp.ui.utils.isAndroid
import com.mdcapp.ui.utils.showToast
import com.mdcapp.ui.viewmodels.invoices.DetailInvoiceViewModel
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    refreshController: RefreshController = koinInject()
) {
    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val tabRoutes = listOf(
        AppRoute.InvoicesPaged.route,
        AppRoute.Agenda.route,
        AppRoute.Commissions.route,
        AppRoute.Clients.route
    )

    val items = listOf(
        NavigationItem("Dashboard", AppRoute.InvoicesPaged.route, Icons.AutoMirrored.Filled.List),
        NavigationItem("Agenda", AppRoute.Agenda.route, Icons.Default.DateRange),
        NavigationItem("Comisiones", AppRoute.Commissions.route, Icons.Default.ShoppingCart),
        NavigationItem("Clientes", AppRoute.Clients.route, Icons.Default.AccountBox)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val isTabActive = currentRoute in tabRoutes

    AppBackHandler {
        if (!isTabActive) {
            navController.popBackStack()
        } else if (currentRoute != AppRoute.InvoicesPaged.route) {
            navController.navigate(AppRoute.InvoicesPaged.route) {
                navController.graph.findStartDestination().route?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                closeApp()
            } else {
                lastBackPressTime = currentTime
                showToast("Presiona de nuevo para salir")
            }
        }
    }

    // Estados para acciones dinámicas
    val showCommissionDatePicker = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isTabActive) {
                TopAppBar(
                    title = {
                        val title = when (currentRoute) {
                            AppRoute.Clients.route -> "Mis Clientes"
                            AppRoute.Agenda.route -> "Agenda de Pagos"
                            AppRoute.Commissions.route -> "Mis Comisiones"
                            else -> "Dashboard Facturas"
                        }
                        Text(title)
                    },
                    actions = {
                        // Acciones dinámicas según la ruta
                        if (currentRoute == AppRoute.Commissions.route) {
                            IconButton(onClick = { showCommissionDatePicker.value = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Rango de Fechas"
                                )
                            }
                        }
                        if (currentRoute == AppRoute.Clients.route) {
                            if (!isAndroid) {
                                IconButton(onClick = { refreshController.triggerRefresh() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                                }
                            }
                            IconButton(onClick = { navController.navigate(AppRoute.Factories.route) }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Gestionar Fábricas"
                                )
                            }
                        }

                        if (currentRoute == AppRoute.InvoicesPaged.route) {
                            if (!isAndroid) {
                                IconButton(onClick = { refreshController.triggerRefresh() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                                }
                            }
                            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Más opciones"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Ver Perfil") },
                                        onClick = {
                                            showMenu = false
                                            navController.navigate(AppRoute.Profile.route)
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Historial de Pagos") },
                                        onClick = {
                                            showMenu = false
                                            navController.navigate(AppRoute.PaymentHistory.route)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.List,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cerrar Sesión") },
                                        onClick = {
                                            showMenu = false
                                            showLogoutDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTabActive) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    navController.graph.findStartDestination().route?.let { route ->
                                        popUpTo(route) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == AppRoute.Clients.route) {
                FloatingActionButton(onClick = { navController.navigate(AppRoute.AddClient.createRoute()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.InvoicesPaged.route,
            enterTransition = {
                if (targetState.destination.route in tabRoutes || !isAndroid) {
                    fadeIn(animationSpec = tween(if (isAndroid) 300 else 150))
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            },
            exitTransition = {
                if (initialState.destination.route in tabRoutes || !isAndroid) {
                    fadeOut(animationSpec = tween(if (isAndroid) 300 else 150))
                } else {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            },
            popEnterTransition = {
                if (targetState.destination.route in tabRoutes || !isAndroid) {
                    fadeIn(animationSpec = tween(if (isAndroid) 300 else 150))
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                }
            },
            popExitTransition = {
                if (initialState.destination.route in tabRoutes || !isAndroid) {
                    fadeOut(animationSpec = tween(if (isAndroid) 300 else 150))
                } else {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                }
            }
        ) {
            composable(AppRoute.InvoicesPaged.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    InvoicesPageScreen(
                        onNavigationInvoice = { num ->
                            navController.navigate(
                                AppRoute.DetailInvoice.createRoute(
                                    num
                                )
                            )
                        }
                    )
                }
            }
            composable(AppRoute.Clients.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    ClientsListScreen(
                        onOrdersClick = { id ->
                            navController.navigate(
                                AppRoute.ClientOrders.createRoute(
                                    id
                                )
                            )
                        },
                        onCurrentAccountClick = { id ->
                            navController.navigate(
                                AppRoute.Invoices.createRoute(
                                    id
                                )
                            )
                        },
                        onEditClientClick = { id, name ->
                            navController.navigate(AppRoute.AddClient.createRoute(id, name))
                        }
                    )
                }
            }
            composable(AppRoute.Agenda.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    AgendaScreen(
                        onInvoiceClick = { num ->
                            navController.navigate(
                                AppRoute.DetailInvoice.createRoute(
                                    num
                                )
                            )
                        }
                    )
                }
            }
            composable(AppRoute.Commissions.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    CommissionsScreen(
                        showDatePickerRequest = showCommissionDatePicker
                    )
                }
            }

            // Detail Screens
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
                        navController.navigate(AppRoute.DetailInvoice.createRoute(invoiceNumber))
                    },
                    onEditOrder = { cId, oId ->
                        navController.navigate(AppRoute.CreateOrder.createRoute(cId, oId))
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
                        navController.navigate(AppRoute.DetailInvoice.createRoute(invoiceNumber))
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class NavigationItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
