package com.mdcapp.data.model


data class OrderModel(
    var orderNumber: String = "", // número de orden
    var nameClient: String = "", // razón social
    var branch: String = "", //marca
    var type: String = "", //tipo de facturación
    var documentDate: String = "", // fecha de facturación
    var numberDocument: String = "", // número de factura
    var trackingState: String = "", // estado de pedido
    var documentComments: String = "", // comentarios
    var sellOut: String = "", // descuento financiero
    var inputDate: String = "", //fecha de carga
    var payState: String = "", // estado de cobranza
    var receptionDate: String = "", // fecha de recepción
    var payDate: String = "", // fecha de pago
    var valueDocument: String = "", // importe de facturación
    var discount: String = "", // valor de descuento
    var payAmount: String = "", // monto a cobrar
    var payedAmount: String = "", //monto pagado
    var payDifference: String = "", // diferencia en pago
    var orders: String = "", // nombre de archivo de pedidos
    var documents: String? = "",// nombre archivo de remitos/facturación
    var checked: String? = "", //nombre de archivo de comprobantes
    var calendar: String? = "", // verificación
    var date: String? = ""

)


