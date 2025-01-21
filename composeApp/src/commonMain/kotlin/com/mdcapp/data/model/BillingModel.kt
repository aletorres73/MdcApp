package com.mdcapp.data.model

data class BillingModel(
    var billingNumber: String = "",
    var orderId: String = "",
    var type: String = "",
    var total: String = "",
    var loadDate: String = "",
    var deliveryDate: String = "",
    var payDate: String = "",
    var articles: List<HashMap<String, String>> = emptyList(),
    var paymentCondition: String
)

data class PaymentCondition(
    val paymentName: String,
    val discount: Double,
    val month: Int,
    val expiration: Int,
    val date: Int,
)
