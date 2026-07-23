package com.mdcapp.data.model

import com.mdcapp.data.remote.RemoteArticle
import com.mdcapp.data.remote.RemoteBillingComments
import com.mdcapp.data.remote.RemoteResultBillingModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val comments: List<BillingComments> = emptyList(),
    val clientName: String = "",
    val timeStamp: Long = 0
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

fun Double.toPrint(): String {
    return "$%,.2f".format(this)
}

fun Double.discountToPrint(): String {
    return "%,.0f".format(this * 100)
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
    comments.map { RemoteBillingComments(it.comments, it.date) },
    clientName,
    timeStamp
)

data class PaymentCondition(
    val paymentName: String = "",
    val discount: Double = 0.0,
    val month: Int = 0,
    val expiration: Int = 0,
    val date: Int = 0,
    val quantity: Int = 0
)

fun PaymentCondition.isEmpty() =
    discount == 0.0 && month == 0 && expiration == 0 && date == 0 && paymentName.isEmpty()

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun BillingModel.recalculate(
    condition: PaymentCondition? = null
): BillingModel {

    val toPayValue = total - discount * total
    val restValue = toPayValue - payed

    val reception = try {
        if (deliveryDate.isNotEmpty())
            LocalDate.parse(deliveryDate, formatter)
        else null
    } catch (e: Exception) {
        null
    }

    val dueDate = reception?.plusDays(condition?.expiration?.toLong() ?: 0)

    val newState = when {
        restValue <= 0 && toPayValue > 0 -> "Cobrado"
        payed > 0 && restValue > 0 -> "En proceso"
        stateBilling.isEmpty() -> "Pendiente"
        else -> stateBilling
    }

    return copy(
        toPay = toPayValue,
        rest = restValue,
        payDate = dueDate?.format(formatter) ?: payDate,
        stateBilling = newState
    )
}

fun LocalDate.formatter(): String {

    val parsed = LocalDate.parse(this.toString()) // ISO por defecto
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    return parsed.format(formatter)

}