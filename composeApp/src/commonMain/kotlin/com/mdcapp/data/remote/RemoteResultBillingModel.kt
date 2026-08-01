package com.mdcapp.data.remote

import com.mdcapp.domain.entities.ArticleModel
import com.mdcapp.domain.entities.BillingComments
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toMoneyDouble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultBillingModel(
    @SerialName("Numero") var billingNumber: String = "",
    @SerialName("Orden") var orderId: String = "",
    @SerialName("Tipo Facturacion") var type: String = "",
    @SerialName("Total") var total: String = "",
    @Serializable(with = FirestoreDateSerializer::class)
    @SerialName("Fecha") var loadDate: Long = 0L,
    @Serializable(with = FirestoreDateSerializer::class)
    @SerialName("Fecha recepción") var deliveryDate: Long = 0L,
    @Serializable(with = FirestoreDateSerializer::class)
    @SerialName("Fecha Pago") var payDate: Long = 0L,
    @SerialName("Articulos") var articles: List<RemoteArticle> = emptyList(),
    @SerialName("Condicion de pago") var paymentCondition: String = "",
    @SerialName("Dto") var expectedDiscount: Double = 0.0,
    @SerialName("A cobrar") var toPay: Double = 0.0,
    @SerialName("Pagado") var payed: String = "",
    @SerialName("Saldo") var rest: String = "",
    @SerialName("Estado") var stateBilling: String = "",
    @SerialName("Cliente Id") var clientId: String = "",
    @SerialName("Marca") var brand: String = "",
    @SerialName("Segmento") var branch: String = "",
    @SerialName("Comentarios") var comments: List<RemoteBillingComments> = emptyList(),
    @SerialName("Razon Social") var clientName: String = "",
    @Serializable(with = FirestoreDateSerializer::class)
    @SerialName("Timestamp") var timeStamp: Long = 0L
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
    @Serializable(with = FirestoreDateSerializer::class)
    val date: Long = 0L
)

fun RemoteArticle.toArticleDomain() = ArticleModel(
    name = name,
    color = color,
    value = value.toMoneyDouble(),
    pairs = pairs.toIntOrNull() ?: 0
)

fun ArticleModel.toArticleRemote() = RemoteArticle(
    name = name,
    color = color,
    value = value.toString(),
    pairs = pairs.toString()
)

fun RemoteResultBillingModel.toBillingDomain(): BillingModel {
    return BillingModel(
        billingNumber = billingNumber,
        orderId = orderId,
        type = type,
        total = total.toMoneyDouble(),
        loadDate = loadDate,
        deliveryDate = deliveryDate,
        payDate = payDate,
        articles = articles.map { it.toArticleDomain() },
        paymentCondition = paymentCondition,
        expectedDiscount = expectedDiscount,
        toPay = toPay,
        payed = payed.toMoneyDouble(),
        rest = rest.toMoneyDouble(),
        stateBilling = stateBilling,
        clientId = clientId,
        brand = brand,
        branch = branch,
        comments = comments.map { BillingComments(it.comments, it.date) },
        clientName = clientName,
        timeStamp = timeStamp
    )
}

fun BillingModel.toBillingRemote() = RemoteResultBillingModel(
    billingNumber = billingNumber,
    orderId = orderId,
    type = type,
    total = total.toString(),
    loadDate = loadDate,
    deliveryDate = deliveryDate,
    payDate = payDate,
    articles = articles.map { it.toArticleRemote() },
    paymentCondition = paymentCondition,
    expectedDiscount = expectedDiscount,
    toPay = toPay,
    payed = payed.toString(),
    rest = rest.toString(),
    stateBilling = stateBilling,
    clientId = clientId,
    brand = brand,
    branch = branch,
    comments = comments.map { RemoteBillingComments(it.comments, it.date) },
    clientName = clientName,
    timeStamp = timeStamp
)

