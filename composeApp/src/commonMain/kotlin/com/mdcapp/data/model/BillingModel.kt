package com.mdcapp.data.model

import com.mdcapp.data.remote.RemoteResultBillingModel

data class BillingModel(
    var billingNumber: String = "",
    var orderId: String = "",
    var type: String = "",
    var total: String = "",
    var loadDate: String = "",
    var deliveryDate: String = "",
    var payDate: String = "",
    var articles: List<HashMap<String, String>> = emptyList(),
    var paymentCondition: String,
    var discount: Double,
    var toPay: Double,
    var payed: String,
    var rest: String,
    var stateBilling: String,
    var clientId: String,
    var brand: String
)

fun BillingModel.toDomain() = RemoteResultBillingModel(
    billingNumber,
    orderId,
    type,
    total,
    loadDate,
    deliveryDate,
    payDate,
    articles,
    paymentCondition,
    discount,
    toPay,
    payed,
    rest,
    stateBilling,
    clientId,
    brand
)

data class PaymentCondition(
    val paymentName: String,
    val discount: Double,
    val month: Int,
    val expiration: Int,
    val date: Int,
)
