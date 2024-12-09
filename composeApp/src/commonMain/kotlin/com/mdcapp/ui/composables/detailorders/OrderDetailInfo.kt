package com.mdcapp.ui.composables.detailorders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.composables.buyorders.BuyOrderItem
import com.mdcapp.ui.viewmodels.buyorders.BuyOrdersViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun OrderDetailInfo(
    order: OrderModel,
    vm: BuyOrdersViewModel = koinViewModel()
) {
    var isBuyOrderClicked by remember { mutableStateOf(false) }
    LaunchedEffect(order.orders) {
        vm.loadHandler("buyOrderDetails", order.orders)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OrderSection(title = "Información de la Orden",
            onClick = { /*vm.loadHandler("orderDetail", order.orderNumber)*/ },
            content = {
                OrderInfoRow(label = "Número de Orden", value = order.orderNumber)
                OrderInfoRow(label = "Razón Social", value = order.nameClient)
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        OrderSection(
            title = "Información del Pedido",
            onClick = { isBuyOrderClicked = !isBuyOrderClicked },
            content = {
                OrderInfoRow(label = "Marca", value = order.branch)
                OrderInfoRow(label = "Comentarios", value = order.documentComments)
                OrderInfoRow(label = "Fecha de Carga", value = order.inputDate)
                OrderInfoRow(label = "Número de Pedido", value = order.orders)
            }
        )
        AnimatedVisibility(isBuyOrderClicked) {
            BuyOrderItem(vm.state.buyOrder)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        OrderSection(
            title = "Información de Facturación y Pagos",
            onClick = { /*onClick("billingsDetail", order.orderNumber)*/ },
            content = {
                OrderInfoRow(label = "Importe total de Facturación", value = order.valueDocument)
                OrderInfoRow(label = "Monto total a Cobrar", value = order.payAmount)
                OrderInfoRow(label = "Monto total Pagado", value = order.payedAmount)
                OrderInfoRow(label = "Diferencia en Pago", value = order.payDifference)
                OrderInfoRow(
                    label = "Archivo de Remitos/Facturación",
                    value = order.documents.orEmpty()
                )
            }
        )
    }
}

