package com.mdcapp.data.model

import com.mdcapp.data.remote.RemoteArticle
import com.mdcapp.data.remote.RemoteBillingComments
import com.mdcapp.data.remote.RemoteResultBillingModel

data class BillingModel(
    val billingNumber: String = "",
    val orderId: String = "",
    val type: String = "",
    val total: Double = 0.0,
    val loadDate: String = "",
    val deliveryDate: String = "",
    val payDate: String = "",
    val articles: List<ArticleModel> = emptyList(),
    val paymentCondition: String = "",
    val discount: Double = 0.0,
    val toPay: Double = 0.0,
    val payed: Double = 0.0,
    val rest: Double = 0.0,
    val stateBilling: String = "",
    val clientId: String = "",
    val brand: String = "",
    val comments: List<BillingComments> = emptyList()
)

data class BillingComments(
    val comments: String = "",
    val date: String = ""
)

data class ArticleModel(
    val name: String = "",
    val color: String = "",
    val value: Double = 0.0,
    val pairs: Int = 0
)

fun String.toMoneyDouble(): Double {
    return this
        .replace("$", "")
        .replace(",", "")
        .trim()
        .toDoubleOrNull() ?: 0.0
}


fun ArticleModel.toRemote() = RemoteArticle(
    name = name,
    color = color,
    value = value.toString(),
    pairs = pairs.toString()
)


fun BillingModel.toRemote() = RemoteResultBillingModel(
    billingNumber,
    orderId,
    type,
    total.toString(),
    loadDate,
    deliveryDate,
    payDate,
    articles.map { it.toRemote() },
    paymentCondition,
    discount,
    toPay,
    payed.toString(),
    rest.toString(),
    stateBilling,
    clientId,
    brand,
    comments.map { RemoteBillingComments(it.comments, it.date) }
)

data class PaymentCondition(
    val paymentName: String,
    val discount: Double,
    val month: Int,
    val expiration: Int,
    val date: Int,
)
