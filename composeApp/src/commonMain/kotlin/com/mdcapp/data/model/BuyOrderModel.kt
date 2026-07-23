package com.mdcapp.data.model

data class BuyOrderModel(
    var id: String = "",
    var clientId: String = "",
    var order: String = "",
    var client: String = "",
    var factory: String = "",
    var branch: String = "",
    var deliveryDate: String = "",
    var type: String = "",
    var billing: String = "",
    var comments: String = "",
    var articles: List<ArticleOrderModel> = emptyList(),
    var loadedDate: String = "",
    var paymentCondition: String = "",
    var discount: Double = 0.0,
    var expirationDays: Int = 0
)

data class ArticleOrderModel(
    val name: String = "",
    val color: String = "",
    val delivered: Int = 0,
    val pairs: Int = 0
)

