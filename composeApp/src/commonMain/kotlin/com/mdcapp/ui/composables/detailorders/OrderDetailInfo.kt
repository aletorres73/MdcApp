package com.mdcapp.ui.composables.detailorders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.OrderModel

@Composable
fun OrderDetailInfo(
    order: OrderModel,
    onClick: (String, String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OrderSection(title = "Información de la Orden",
            onClick = { onClick("orderDetail", order.orderNumber) },
            content = {
                OrderInfoRow(label = "Número de Orden", value = order.orderNumber)
                OrderInfoRow(label = "Razón Social", value = order.nameClient)
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        OrderSection(
            title = "Información del Pedido",
            onClick = { onClick("buyOrderDetails", order.orderNumber) },
            content = {
                OrderInfoRow(label = "Marca", value = order.branch)/*
            OrderInfoRow(
                label = "Estado de Pedido",
                value = order.trackingState
            )*/
                OrderInfoRow(label = "Comentarios", value = order.documentComments)
                OrderInfoRow(label = "Fecha de Carga", value = order.inputDate)
                OrderInfoRow(label = "Número de Pedido", value = order.orders)
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        OrderSection(
            title = "Información de Facturación y Pagos",
            onClick = { onClick("billingsDetail", order.orderNumber) },
            content = {
//            OrderInfoRow(label = "Número de Factura", value = order.numberDocument)
//            OrderInfoRow(label = "Fecha de Facturación", value = order.documentDate)
//            OrderInfoRow(label = "Tipo de Facturación", value = order.type)
                OrderInfoRow(label = "Importe total de Facturación", value = order.valueDocument)
//                OrderInfoRow(label = "Estado de Cobranza", value = order.payState)
//                OrderInfoRow(label = "Fecha de Recepción", value = order.receptionDate)
//                OrderInfoRow(label = "Fecha de Pago", value = order.payDate)
//            OrderInfoRow(label = "Valor de Descuento", value = order.discount)
//            OrderInfoRow(label = "Descuento total", value = "%${order.sellOut.toInt()*100}")
                OrderInfoRow(label = "Monto total a Cobrar", value = order.payAmount)
                OrderInfoRow(label = "Monto total Pagado", value = order.payedAmount)
                OrderInfoRow(label = "Diferencia en Pago", value = order.payDifference)
                OrderInfoRow(
                    label = "Archivo de Remitos/Facturación",
                    value = order.documents.orEmpty()
                )
//                OrderInfoRow(label = "Archivo de Comprobantes", value = order.checked.orEmpty())
//            OrderInfoRow(label = "Verificación", value = order.calendar.orEmpty())
//            OrderInfoRow(label = "Fecha", value = order.date.orEmpty())
            }
        )
    }
}


