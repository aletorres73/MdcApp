package com.mdcapp.data.remote

import com.mdcapp.data.model.ArticleModel
import com.mdcapp.data.model.BillingComments
import com.mdcapp.data.model.BillingModel
import com.mdcapp.data.model.toMoneyDouble
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
    @SerialName("Articulos") var articles: List<RemoteArticle> = emptyList(),
    @SerialName("Condicion de pago") var paymentCondition: String = "",
    @SerialName("Dto") var discount: Double = 0.0,
    @SerialName("A cobrar") var toPay: Double = 0.0,
    @SerialName("Pagado") var payed: String = "",
    @SerialName("Saldo") var rest: String = "",
    @SerialName("Estado") var stateBilling: String = "",
    @SerialName("Cliente Id") var clientId: String = "",
    @SerialName("Marca") var brand: String = "",
    @SerialName("Comentarios") var comments: List<RemoteBillingComments> = emptyList()
)

@Serializable
data class RemoteArticle(
    @SerialName("Articulo") val name: String = "",
    @SerialName("Color") val color: String = "",
    @SerialName("Importe") val value: String = "",
    @SerialName("Pares") val pairs: String = ""
)

@Serializable
data class RemoteBillingComments(
    val comments: String = "",
    val date: String = ""
)

fun RemoteArticle.toDomain() = ArticleModel(
    name = name,
    color = color,
    value = value.toMoneyDouble(),
    pairs = pairs.toIntOrNull() ?: 0
)


fun RemoteResultBillingModel.toDomain(): BillingModel {
    return BillingModel(
        billingNumber = billingNumber,
        orderId = orderId,
        type = type,
        total = total.toMoneyDouble(),
        loadDate = loadDate,
        deliveryDate = deliveryDate,
        payDate = payDate,
        articles = articles.map { it.toDomain() },
        paymentCondition = paymentCondition,
        discount = discount,
        toPay = toPay,
        payed = payed.toMoneyDouble(),
        rest = rest.toMoneyDouble(),
        stateBilling = stateBilling,
        clientId = clientId,
        brand = brand,
        comments = comments.map { BillingComments(it.comments, it.date) }
    )
}
