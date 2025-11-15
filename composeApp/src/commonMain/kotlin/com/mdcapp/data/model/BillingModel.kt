package com.mdcapp.data.model

import com.mdcapp.data.remote.RemoteArticle
import com.mdcapp.data.remote.RemoteResultBillingModel

data class BillingModel(
    val billingNumber: String = "",
    val orderId: String = "",
    val type: String = "",
    val total: String = "",
    val loadDate: String = "",
    val deliveryDate: String = "",
    val payDate: String = "",
    val articles: List<ArticleModel> = emptyList(),
    val paymentCondition: String = "",
    val discount: Double = 0.0,
    val toPay: Double = 0.0,
    val payed: String = "",
    val rest: String = "",
    val stateBilling: String = "",
    val clientId: String = "",
    val brand: String = ""
)

data class ArticleModel(
    val name: String = "",
    val color: String = "",
    val delivered: Int = 0,
    val pairs: Int = 0
)

fun ArticleModel.toRemote() = RemoteArticle(
    name = name,
    color = color,
    delivered = delivered.toString(),
    pairs = pairs.toString()
)


fun BillingModel.toRemote() = RemoteResultBillingModel(
    billingNumber,
    orderId,
    type,
    total,
    loadDate,
    deliveryDate,
    payDate,
    articles.map { it.toRemote() },
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
