package com.mdcapp.data.remote

import com.mdcapp.data.model.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultOrder(
    @SerialName("Pedido Id") var id: String,
    @SerialName("Orden Id") var order: String,
    @SerialName("Razon Social") var client: String,
    @SerialName("Marca") var branch: String,
    @SerialName("Plazo de entrega") var deliveryDate: String,
    @SerialName("Tipo") var type: String,
    @SerialName("Facturación") var billing: String,
    @SerialName("Comentarios") var coments: String,
    @SerialName("Articulos") var articles: HashMap<String, String>,
    @SerialName("Fecha de carga") var loadedDate: String,
)

fun RemoteResultOrder.toDomain() = Order(
    id = id,
    order = order,
    client = client,
    branch = branch,
    deliveryDate = deliveryDate,
    type = type,
    billing = billing,
    comments = coments,
    articles = articles,
    loadedDate = loadedDate
)