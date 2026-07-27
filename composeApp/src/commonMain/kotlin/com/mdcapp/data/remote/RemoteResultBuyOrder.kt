package com.mdcapp.data.remote

import com.mdcapp.domain.entities.ArticleOrderModel
import com.mdcapp.domain.entities.BuyOrderModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultBuyOrder(
    @SerialName("Pedido Id") var id: String = "",
    @SerialName("Orden Id") var order: String = "",
    @SerialName("Cliente Id") var clientId: String = "",
    @SerialName("Razón Social") var client: String = "",
    @SerialName("Fábrica") var factory: String = "",
    @SerialName("Marca") var branch: String = "",
    @SerialName("Plazo de entrega") var deliveryDate: String = "",
    @SerialName("Tipo") var type: String = "",
    @SerialName("Facturación") var billing: String = "",
    @SerialName("Comentarios") var comments: String = "",
    @SerialName("Articulos") var articles: List<RemoteArticleOrderModel> = emptyList(),
    @SerialName("Fecha de carga") var loadedDate: String = "",
    @SerialName("Condición de Pago") var paymentCondition: String = "",
    @SerialName("Descuento") var discount: Double = 0.0,
    @SerialName("Días Vencimiento") var expirationDays: Int = 0
)

fun RemoteResultBuyOrder.toBuyOrderDomain() = BuyOrderModel(
    id = id,
    clientId = clientId,
    order = order,
    client = client,
    factory = factory,
    branch = branch,
    deliveryDate = deliveryDate,
    type = type,
    billing = billing,
    comments = comments,
    articles = articles.map { it.toArticleOrderDomain() },
    loadedDate = loadedDate,
    paymentCondition = paymentCondition,
    discount = discount,
    expirationDays = expirationDays
)

fun BuyOrderModel.toBuyOrderRemote() = RemoteResultBuyOrder(
    id = id,
    clientId = clientId,
    order = order,
    client = client,
    factory = factory,
    branch = branch,
    deliveryDate = deliveryDate,
    type = type,
    billing = billing,
    comments = comments,
    articles = articles.map { it.toArticleOrderRemote() },
    loadedDate = loadedDate,
    paymentCondition = paymentCondition,
    discount = discount,
    expirationDays = expirationDays
)

@Serializable
data class RemoteArticleOrderModel(
    @SerialName("Articulo") val name: String = "",
    @SerialName("Color") val color: String = "",
    @SerialName("Entregados") val delivered: String = "",
    @SerialName("Pares") val pairs: String = ""
)

fun RemoteArticleOrderModel.toArticleOrderDomain() = ArticleOrderModel(
    name,
    color,
    delivered.toIntOrNull() ?: 0,
    pairs.toIntOrNull() ?: 0
)

fun ArticleOrderModel.toArticleOrderRemote() = RemoteArticleOrderModel(
    name = name,
    color = color,
    delivered = delivered.toString(),
    pairs = pairs.toString()
)

