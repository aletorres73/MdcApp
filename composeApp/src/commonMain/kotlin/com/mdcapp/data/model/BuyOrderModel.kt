package com.mdcapp.data.model

data class BuyOrderModel(
    var id: String = "",
    var clientId: String = "",
    var order: String = "",
    var client: String = "",
    var branch: String = "",
    var deliveryDate: String = "",
    var type: String = "",
    var billing: String = "",
    var comments: String = "",
    var articles: List<ArticleOrderModel> = emptyList(),
    var loadedDate: String = "",
)

data class ArticleOrderModel(
    val name: String = "",
    val color: String = "",
    val delivered: Int = 0,
    val pairs: Int = 0
)

