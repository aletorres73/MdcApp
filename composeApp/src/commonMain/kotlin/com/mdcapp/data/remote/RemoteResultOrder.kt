package com.mdcapp.data.remote

import com.mdcapp.data.model.OrderModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultOrder(
    @SerialName("N° ")
    var orderNumber: String = "", // número de orden
    @SerialName("Razón Social")
    var nameClient: String = "", // razón social
    @SerialName("Marca")
    var branch: String = "", //marca
    @SerialName("Tipo")
    var type: String = "", //tipo de facturación
    @SerialName("Fecha Remito/Factura")
    var documentDate: String = "", // fecha de facturación
    @SerialName("Estado de despacho")
    var trackingState: String = "", // estado de pedido
    @SerialName("Estado de cobranza")
    var payState: String = "", // estado de cobranza
    @SerialName("Importe fc/rt")
    var valueDocument: String = "", // importe de facturación
    @SerialName("Pedidos")
    var orders: String = "", // nombre de archivo de pedidos
)

fun RemoteResultOrder.toDomain() = OrderModel(
    orderNumber = orderNumber,
    nameClient = nameClient,
    branch = branch,
    type = type,
    documentDate = documentDate,
    trackingState = trackingState,
    payState = payState,
    valueDocument = valueDocument,
    orders = orders
)