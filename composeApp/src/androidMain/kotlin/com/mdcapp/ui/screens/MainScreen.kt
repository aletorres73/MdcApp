package com.mdcapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onNavigateToClientInvoices: (String) -> Unit,
    onNavigateToAddClient: () -> Unit,
    onNavigateToEditClient: (String, String) -> Unit,
    onNavigateToCreateOrder: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        NavigationItem("Facturas", AppRoute.InvoicesPaged.route, Icons.Default.List),
        NavigationItem("Clientes", AppRoute.Clients.route, Icons.Default.AccountBox)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MDC App") },
                actions = {
                    IconButton(onClick = onLogout) {
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
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.InvoicesPaged.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.InvoicesPaged.route) {
                InvoicesPageScreen(
                    onNavigationInvoice = onNavigateToInvoice,
                    onNavigationClientDetail = onNavigateToClientInvoices,
                    onAddClientClick = onNavigateToAddClient,
                    onCreateOrderClick = onNavigateToCreateOrder
                )
            }
            composable(AppRoute.Clients.route) {
                ClientsListScreen(
                    onAddClientClick = onNavigateToAddClient,
                    onEditClientClick = onNavigateToEditClient
                )
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
