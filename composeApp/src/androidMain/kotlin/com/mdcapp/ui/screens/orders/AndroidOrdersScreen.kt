package com.mdcapp.ui.screens.orders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(
    KoinExperimentalAPI::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun AndroidOrdersScreen(
    vm: OrdersViewModel = koinViewModel(),
    factoryName: String
) {
    LaunchedEffect(factoryName) {
        vm.init(if (factoryName == "IBA") "Gummi" else factoryName)
    }
    Screen {
        val state = vm.state
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            topBar = {
                MainTopAppBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    titleContent = {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "MDC Ordenes \t $factoryName",
                                modifier = Modifier.padding(vertical = 6.dp),
                                style = MaterialTheme.typography.titleMediumEmphasized
                            )
                            HorizontalDivider()
                            FilterTextButtons { filter, isPressed ->
                                vm.filterListByOrderState(filter, isPressed)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { padding ->
            val refreshState = rememberPullToRefreshState()
            val scope = rememberCoroutineScope()
            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = { scope.launch { vm.fetchFromRepository() } },
                state = refreshState,
                indicator = { LoadingIndicator(enabled = state.loading) }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(state.orderList, key = { it.orderNumber }) {
                        OrderItems(
                            order = it,
                            onCardClick = {}
                        )
                    }
                }
                LoadingIndicator(enabled = state.loading)
            }
        }
    }
}



