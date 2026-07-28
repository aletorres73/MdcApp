package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    onManageFactories: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val items = listOf(
        NavigationItem("Facturas", AppRoute.InvoicesPaged.route, Icons.Default.List),
        NavigationItem("Clientes", AppRoute.Clients.route, Icons.Default.AccountBox)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (currentRoute == AppRoute.Clients.route) "Mis Clientes" else "Dashboard Facturas")
                },
                actions = {
                    IconButton(onClick = onManageFactories) {
                        Icon(Icons.Default.Settings, contentDescription = "Gestionar Fábricas")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
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

