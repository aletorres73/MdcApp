package com.mdcapp.data.remote

import com.mdcapp.data.model.BillingModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultBillingModel(
    @SerialName("Numero") var billingNumber: String = "",
    @SerialName("Orden") var orderId: String = "",
    @SerialName("Tipo Facturacion") var type: String = "",
    @SerialName("Total") var total: String = "",
    @SerialName("Fecha") var loadDate: String = "",
    @SerialName("Fecha recepción") var deliveryDate: String = "",
    @SerialName("Fecha Pago") var payDate: String = "",
    @SerialName("Articulos") var articles: List<HashMap<String, String>> = emptyList(),
    @SerialName("Condicion de pago") var paymentCondition: String = "",
    @SerialName("Dto") var discount: Double = 0.0,
    @SerialName("A cobrar") var toPay: Double = 0.0,
    @SerialName("Pagado") var payed: Double = 0.0,
    @SerialName("Saldo") var rest: Double = 0.0
)

fun RemoteResultBillingModel.toDomain() = BillingModel(
    billingNumber = billingNumber,
    orderId = orderId,
    type = type,
    total = total,
    loadDate = loadDate,
    deliveryDate = deliveryDate,
    payDate = payDate,
    articles = articles,
    paymentCondition = paymentCondition,
    discount = discount,
    toPay = discount,
    payed = payed,
    rest = rest
)