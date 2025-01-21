package com.mdcapp.data.model

data class BuyOrderModel(
    var id: String = "",
    var order: String = "",
    var client: String = "",
    var branch: String = "",
    var deliveryDate: String = "",
    var type: String = "",
    var billing: String = "",
    var comments: String = "",
    var articles: List<HashMap<String, String>> = emptyList(),
    var loadedDate: String = "",
)