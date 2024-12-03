package com.mdcapp.data.remote


import com.google.firebase.firestore.PropertyName
import com.mdcapp.data.model.Order
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultOrder(
    @get:PropertyName("Pedido Id")
    @set:PropertyName("Pedido ID")
    var id: String,

    @get:PropertyName("Orden")
    @set:PropertyName("Orden")
    var order: String,

    @get:PropertyName("Razon Social")
    @set:PropertyName("Razon Social")
    var client: String,

    @get:PropertyName("Marca")
    @set:PropertyName("Marca")
    var branch: String,

    @get:PropertyName("Plazo de entrega")
    @set:PropertyName("Plazo de entrega")
    var deliveryDate: String,

    @get:PropertyName("Tipo")
    @set:PropertyName("Tipo")
    var type: String,

    @get:PropertyName("Facturación")
    @set:PropertyName("Facturación")
    var billing: String,

    @get:PropertyName("Comentarios")
    @set:PropertyName("Comentarios")
    var coments: String,

    @get:PropertyName("Articulos")
    @set:PropertyName("Articulos")
    var articles: HashMap<String, String>,

    @get:PropertyName("Fecha de carga")
    @set:PropertyName("Fecha de carga")
    var loadedDate: String,

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