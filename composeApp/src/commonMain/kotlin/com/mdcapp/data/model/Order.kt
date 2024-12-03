package com.mdcapp.data.model

data class Order(
    var id: String,
    var order: String = "",
    var client: String = "",
    var branch: String = "",
    var deliveryDate: String = "",
    var type: String = "",
    var billing: String = "",
    var comments: String = "",
    var articles: HashMap<String, String> = hashMapOf("" to ""),
    var loadedDate: String = "",
)

val orderList = (1..10).map {
    Order(
        id = it.toString(),
        order= "Order$it"
    )
}