package com.mdcapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AndroidOrdersScreen(
    vm: OrdersViewModel = koinViewModel()
) {
    Screen {
        val state = vm.state
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(state.orderList, key = { it.orderNumber }) {
                    OrderItems(
                        order = it
                    )
                }
            }
        }
    }
}

