package com.mdcapp.data.remote

import com.mdcapp.data.model.ArticleOrderModel
import com.mdcapp.data.model.BuyOrderModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultBuyOrder(
    @SerialName("Pedido Id") var id: String = "",
    @SerialName("Orden Id") var order: String = "",
    @SerialName("Razon Social") var client: String = "",
    @SerialName("Marca") var branch: String = "",
    @SerialName("Plazo de entrega") var deliveryDate: String = "",
    @SerialName("Tipo") var type: String = "",
    @SerialName("Facturación") var billing: String = "",
    @SerialName("Comentarios") var comments: String = "",
    @SerialName("Articulos") var articles: List<RemoteArticleOrderModel> = emptyList(),
    @SerialName("Fecha de carga") var loadedDate: String = "",
)

fun RemoteResultBuyOrder.toDomain() = BuyOrderModel(
    id = id,
    order = order,
    client = client,
    branch = branch,
    deliveryDate = deliveryDate,
    type = type,
    billing = billing,
    comments = comments,
    articles = articles.map { it.toDomain() },
    loadedDate = loadedDate
)

fun BuyOrderModel.toRemote() = RemoteResultBuyOrder(
    id = id,
    order = order,
    client = client,
    branch = branch,
    deliveryDate = deliveryDate,
    type = type,
    billing = billing,
    comments = comments,
    articles = articles.map { it.toRemote() },
    loadedDate = loadedDate
)

@Serializable
data class RemoteArticleOrderModel(
    @SerialName("Articulo") val name: String = "",
    @SerialName("Color") val color: String = "",
    @SerialName("Entregados") val delivered: String = "",
    @SerialName("Pares") val pairs: String = ""
)

fun RemoteArticleOrderModel.toDomain() = ArticleOrderModel(
    name,
    color,
    delivered.toIntOrNull() ?: 0,
    pairs.toIntOrNull() ?: 0
)

fun ArticleOrderModel.toRemote() = RemoteArticleOrderModel(
    name = name,
    color = color,
    delivered = delivered.toString(),
    pairs = pairs.toString()
)
