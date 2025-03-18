package com.mdcapp.ui.screens.orders

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.common.SearchButton
import com.mdcapp.ui.composables.common.SearchbarTopBar
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(
    KoinExperimentalAPI::class, ExperimentalMaterial3Api::class,
)
@Composable
fun AndroidOrdersScreen(
    vm: OrdersViewModel = koinViewModel(),
    factoryName: String,
    onDetailClick: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    LaunchedEffect(factoryName) {
        vm.init(if (factoryName == "IBA") "Gummi" else factoryName)
    }

    Screen {
        val state = vm.state
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
//        var isSearchBar by remember { mutableStateOf(false) }
        val isSearchBar = vm.state.isSearchBar
        var resetFilters by remember { mutableStateOf(false) }

        BackHandler(enabled = isSearchBar) {
//            isSearchBar = false
            vm.setSearchBar(false)
            resetFilters = true
            vm.cleanSearchQuery()
        }

        Scaffold(
            topBar = {
                AnimatedVisibility(visible = !isSearchBar) {
                    MainTopAppBar(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "MDC App",
                        onActions = {
                            SearchButton(false, onSearchIconClick = { vm.setSearchBar(true) })
                        },
                        scrollBehavior = scrollBehavior,
                        onBack = onBackPressed,
                        isNavigationOn = true
                    )
                }
                AnimatedVisibility(visible = isSearchBar) {
                    val query = state.query
                    SearchbarTopBar(
                        query = query,
                        onQueryChange = { newQuery -> vm.searchOrders(newQuery) },
                        onCleanQuery = {},
                        onClose = {
//                            isSearchBar = false
                            vm.setSearchBar(false)
                            resetFilters = true
                            vm.cleanSearchQuery()
                        },
                    )
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { padding ->
            val refreshState = rememberPullToRefreshState()
            val scope = rememberCoroutineScope()
            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = { scope.launch { vm.fetchFromRepository() } },
                state = refreshState,
                indicator = { LoadingIndicator(enabled = state.loading) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    HorizontalDivider()
                    FilterTextButtons(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        reset = resetFilters,
                        onReset = { resetFilters = false },
                        onFilterPressed = { filter, isPressed ->
                            vm.filterListByOrderState(filter, isPressed)
                        }
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(state.orderList, key = { it.orderNumber }) { order ->
                            val branch = vm.branch[order.orderNumber]
                            if (branch == null) {
                                // Iniciar la descarga de la marca si no está cargada
                                vm.getBranchOrder(order.orderNumber)
                            }
                            OrderItems(
                                order = order,
                                orderBranch = branch ?: "",
//                                billingList = emptyList(),
                                onCardClick = {
                                    onDetailClick(order.orderNumber)
                                }
                            )
                        }
                    }
                }
                LoadingIndicator(enabled = state.loading)
            }
        }
    }
}



