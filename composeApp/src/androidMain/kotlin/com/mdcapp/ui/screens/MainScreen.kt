package com.mdcapp.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mdcapp.domain.entities.AppRoute
import com.mdcapp.ui.screens.invoicesPage.InvoicesPageScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToInvoice: (String) -> Unit,
    onNavigateToClientOrders: (String) -> Unit,
    onNavigateToCurrentAccount: (String) -> Unit,
    onNavigateToAddClient: () -> Unit,
    onNavigateToEditClient: (String, String) -> Unit,
    onNavigateToCreateOrder: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onManageFactories: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
        }
    }

    val items = listOf(
        NavigationItem("Dashboard", AppRoute.InvoicesPaged.route, Icons.AutoMirrored.Filled.List),
        NavigationItem("Agenda", AppRoute.Agenda.route, Icons.Default.DateRange),
        NavigationItem("Comisiones", AppRoute.Commissions.route, Icons.Default.ShoppingCart),
        NavigationItem("Clientes", AppRoute.Clients.route, Icons.Default.AccountBox)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Estados para acciones dinámicas
    var showCommissionDatePicker = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
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
                            Icon(Icons.Default.DateRange, contentDescription = "Rango de Fechas")
                        }
                    }
                    if (currentRoute == AppRoute.Clients.route)
                        IconButton(onClick = onManageFactories) {
                            Icon(Icons.Default.Settings, contentDescription = "Gestionar Fábricas")
                        }

                    if (currentRoute == AppRoute.InvoicesPaged.route) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ver Perfil") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToProfile()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
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
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == AppRoute.Clients.route) {
                FloatingActionButton(onClick = onNavigateToAddClient) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.InvoicesPaged.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.InvoicesPaged.route) {
                InvoicesPageScreen(
                    onNavigationInvoice = onNavigateToInvoice
                )
            }
            composable(AppRoute.Clients.route) {
                ClientsListScreen(
                    onOrdersClick = onNavigateToClientOrders,
                    onCurrentAccountClick = onNavigateToCurrentAccount,
                    onEditClientClick = onNavigateToEditClient
                )
            }
            composable(AppRoute.Agenda.route) {
                AgendaScreen(
                    onInvoiceClick = onNavigateToInvoice
                )
            }
            composable(AppRoute.Commissions.route) {
                CommissionsScreen(
                    showDatePickerRequest = showCommissionDatePicker
                )
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

