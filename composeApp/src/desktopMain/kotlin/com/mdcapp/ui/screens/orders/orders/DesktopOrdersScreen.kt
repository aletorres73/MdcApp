package com.mdcapp.ui.screens.orders.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.common.SearchbarTopBar
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun DesktopOrdersScreen(
    vm: OrdersViewModel = koinViewModel(),
    onOpenOrderDetail: (order: OrderModel, factoryName: String) -> Unit
) {
    val state = vm.state
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isSearchBar = vm.state.isSearchBar

    var resetFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.init("all")
        vm.setSearchBar(false)
        resetFilters = true
        vm.cleanSearchQuery()
    }
    Screen {
        Scaffold(
            topBar = {
                AnimatedVisibility(visible = !isSearchBar) {
                    MainTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        titleContent = {
                            Text(
                                "MDC Ordenes",
                                modifier = Modifier.padding(6.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onActions = {
                            IconButton(onClick = { vm.setSearchBar(true) }) {
                                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        isNavigationOn = false
                    )
                }
                AnimatedVisibility(visible = isSearchBar) {
                    val query = state.query
                    SearchbarTopBar(
                        query = query,
                        onQueryChange = { newQuery -> vm.searchOrders(newQuery, true) },
                        onCleanQuery = {},
                        onClose = {
                            vm.setSearchBar(false)
//                            resetFilters = true
                            vm.cleanSearchQuery()
                        },
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilterFactoriesButtons(
                    factories = state.factoriesList,
                    onFilterPressed = { factory, isPressed ->
                        println("$factory: $isPressed")
                        vm.filterOrdersByFactory(factory, isPressed)
                    },
                    onReset = { resetFilters = false },
                    reset = resetFilters
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(350.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxHeight()
//                        .padding(padding)
                ) {
                    items(state.orderList, key = null) { order ->
                        OrderItems(
                            order = order,
                            onCardClick = {
                                onOpenOrderDetail(
                                    order,
                                    order.branch
                                )
                            },
                            orderBranch = order.branch
                        )
                    }
                }
            }
        }
    }
}



