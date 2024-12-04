package com.mdcapp.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DesktopOrdersScreen(
    vm: OrdersViewModel = koinViewModel()
) {
    Screen {
        val state = vm.state
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(padding)

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


