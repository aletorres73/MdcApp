package com.mdcapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.common.MainTopAppBar
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun AndroidOrdersScreen(
    vm: OrdersViewModel = koinViewModel()
) {
    Screen {
        val state = vm.state
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            topBar = {
                MainTopAppBar(
                    title = "MDC Ordenes",
                    scrollBehavior = scrollBehavior
                ) {
                    FilterTextButtons { filter, isPressed ->
                        vm.filterListByOrderState(filter, isPressed)
                    }
                }

            },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { padding ->
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding,
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(state.orderList, key = { it.orderNumber }) {
                        OrderItems(
                            order = it,
                            onCardClick = {}
                        )
                    }
                }

            }
        }
    }
}


