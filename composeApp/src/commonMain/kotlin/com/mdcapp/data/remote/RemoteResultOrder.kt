package com.mdcapp.data.remote

import com.mdcapp.domain.entities.OrderModel
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
    var documentDate: Long = 0L, // fecha de facturación
    @SerialName("N° Factura/Remito")
    var numberDocument: String = "", // número de factura
    @SerialName("Estado de despacho")
    var trackingState: String = "", // estado de pedido
    @SerialName("Comentarios")
    var documentComments: String = "", // comentarios
    @SerialName("Descuentos")
    var sellOut: String = "", // descuento financiero
    @SerialName("Fecha de carga")
    var inputDate: Long = 0L, //fecha de carga
    @SerialName("Estado de cobranza")
    var payState: String = "", // estado de cobranza
    @SerialName("Fecha recepción")
    var receptionDate: Long = 0L, // fecha de recepción
    @SerialName("Fecha de pago")
    var payDate: Long = 0L, // fecha de pago
    @SerialName("Importe fc/rt")
    var valueDocument: String = "", // importe de facturación
    @SerialName("Desc / Dev")
    var discount: String = "", // valor de descuento
    @SerialName("Monto a cobrar")
    var payAmount: String = "", // monto a cobrar
    @SerialName("Monto cobrado")
    var payedAmount: String = "", //monto pagado
    @SerialName("Diferencia")
    var payDifference: String = "", // diferencia en pago
    @SerialName("Pedidos")
    var orders: String = "", // nombre de archivo de pedidos
    @SerialName("Remitos/ Facturas")
    var documents: String? = "",// nombre archivo de remitos/facturación
    @SerialName("Comprobantes")
    var checked: String? = "", //nombre de archivo de comprobantes
    @SerialName("Calendario")
    var calendar: String? = "", // verificación
    @SerialName("Plazo")
    var date: Long = 0L
)

fun RemoteResultOrder.toOrderDomain() = OrderModel(
    orderNumber = orderNumber,
    nameClient = nameClient,
    branch = branch,
    type = type,
    documentDate = documentDate,
    numberDocument = numberDocument,
    trackingState = trackingState,
    documentComments = documentComments,
    sellOut = sellOut,
    inputDate = inputDate,
    payState = payState,
    receptionDate = receptionDate,
    payDate = payDate,
    valueDocument = valueDocument,
    discount = discount,
    payAmount = payAmount,
    payedAmount = payedAmount,
    payDifference = payDifference,
    orders = orders,
    documents = documents,
    checked = checked,
    calendar = calendar,
    date = date
)
