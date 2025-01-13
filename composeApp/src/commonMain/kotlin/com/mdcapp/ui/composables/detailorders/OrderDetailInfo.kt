package com.mdcapp.ui.composables.detailorders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.BillingModel
import com.mdcapp.ui.composables.billings.BillingList
import com.mdcapp.ui.composables.buyorders.BuyOrderItem
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun OrderDetailInfo(
    orderId: String,
    vm: BuyOrdersViewModel = koinViewModel(),
    onBillingClicked: (BillingModel) -> Unit
) {
    val state = vm.state
    var isBuyOrderClicked by remember { mutableStateOf(false) }
    var isBillingClicked by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        vm.init(orderId)
    }

    Column(
        modifier = Modifier.padding(
            horizontal = 8.dp,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Pedido:", style = MaterialTheme.typography.titleMedium)
        OrderSection(
            onClick = { isBuyOrderClicked = !isBuyOrderClicked },
            content = {
                OrderInfoRow(label = "Razón Social", value = state.buyOrder.client)
                OrderInfoRow(label = "Marca", value = state.buyOrder.branch)
                OrderInfoRow(label = "Comentarios", value = state.buyOrder.comments)
                OrderInfoRow(label = "Fecha de Carga", value = state.buyOrder.loadedDate)
                OrderInfoRow(label = "Número de Pedido", value = state.buyOrder.id)
            }
        )
        AnimatedVisibility(isBuyOrderClicked) {
            BuyOrderItem(vm.state.buyOrder)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Facturación:", style = MaterialTheme.typography.titleMedium)
        OrderSection(
            onClick = { isBillingClicked = !isBillingClicked },
            content = {
                OrderInfoRow(
                    label = "Importe total ",
                    value = "$" + String.format("%.2f", state.totalAmount)
                )
            }
        )
        AnimatedVisibility(isBillingClicked) {
            BillingList(
                billings = vm.state.billings,
                onBillingClicked = { billing -> onBillingClicked(billing) }
            )
        }
    }
}

