package com.mdcapp.ui.screens.orderdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdcapp.data.model.OrderModel
import com.mdcapp.ui.Screen

@Composable
fun DesktopOrderDetailScreen(
    order: OrderModel
) {
    Screen { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OrderInfo(order = order)
        }
    }
}

@Composable
fun OrderInfo(order: OrderModel) {
    val orderFields = listOf(
        "Número de Orden" to order.orderNumber,
        "Razón Social" to order.nameClient,
        "Marca" to order.branch,
        "Tipo de Facturación" to order.type,
        "Fecha de Facturación" to order.documentDate,
        "Número de Factura" to order.numberDocument,
        "Estado de Pedido" to order.trackingState,
        "Comentarios" to order.documentComments,
        "Descuento Financiero" to order.sellOut,
        "Fecha de Carga" to order.inputDate,
        "Estado de Cobranza" to order.payState,
        "Fecha de Recepción" to order.receptionDate,
        "Fecha de Pago" to order.payDate,
        "Importe de Facturación" to order.valueDocument,
        "Valor de Descuento" to order.discount,
        "Monto a Cobrar" to order.payAmount,
        "Monto Pagado" to order.payedAmount,
        "Diferencia en Pago" to order.payDifference,
        "Archivo de Pedidos" to order.orders,
        "Archivo de Remitos/Facturación" to (order.documents ?: ""),
        "Archivo de Comprobantes" to (order.checked ?: ""),
        "Verificación" to (order.calendar ?: ""),
        "Fecha" to (order.date ?: "")
    )
    orderFields.forEach { (label, value) ->
        OrderInfoRow(label = label, value = value)
    }
}

@Composable
fun OrderInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

