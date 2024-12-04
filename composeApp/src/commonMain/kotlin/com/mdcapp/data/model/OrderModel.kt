package com.mdcapp.data.model


data class OrderModel(
    var orderNumber: String = "", // número de orden
    var nameClient: String = "", // razón social
    var branch: String = "", //marca
    var type: String = "", //tipo de facturación
    var documentDate: String = "", // fecha de facturación
    var trackingState: String = "", // estado de pedido
    var payState: String = "", // estado de cobranza
    var valueDocument: String = "", // importe de facturación
    var orders: String = "", // nombre de archivo de pedidos
)

