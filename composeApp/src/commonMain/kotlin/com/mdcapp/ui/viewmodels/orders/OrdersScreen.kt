package com.mdcapp.ui.viewmodels.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.Order
import com.mdcapp.ui.Screen
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun OrdersScreen(
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
                items(state.orderList, key = null) {
                    OrderItems(
                        order = it
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItems(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(text = "Orden ${order.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = order.client, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
