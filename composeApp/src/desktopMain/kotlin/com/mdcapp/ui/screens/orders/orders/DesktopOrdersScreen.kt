package com.mdcapp.ui.screens.orders.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.Screen
import com.mdcapp.ui.composables.orders.OrderItems
import com.mdcapp.ui.viewmodels.orders.OrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DesktopOrdersScreen(
    vm: OrdersViewModel = koinViewModel(),
    onOpenOrderDetail: (OrderModel) -> Unit
) {
    Screen {
        Scaffold { padding ->
            val state = vm.state
            LazyVerticalGrid(
                columns = GridCells.Adaptive(250.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(padding)
            ) {
                items(state.orderList, key = null) { order ->
                    OrderItems(
                        order = order,
                        onCardClick = { onOpenOrderDetail(order) }
                    )
                }
            }
        }
    }
}



