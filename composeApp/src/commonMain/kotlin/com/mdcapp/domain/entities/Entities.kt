package com.mdcapp.domain.entities

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

data class FactoryModel(
    val name: String,
    val branchList: List<String>,
    val paymentType: List<PaymentCondition>
)

data class OrderModel(
    var orderNumber: String = "",
    var nameClient: String = "",
    var branch: String = "",
    var type: String = "",
    var documentDate: String = "",
    var numberDocument: String = "",
    var trackingState: String = "",
    var documentComments: String = "",
    var sellOut: String = "",
    var inputDate: String = "",
    var payState: String = "",
    var receptionDate: String = "",
    var payDate: String = "",
    var valueDocument: String = "",
    var discount: String = "",
    var payAmount: String = "",
    var payedAmount: String = "",
    var payDifference: String = "",
    var orders: String = "",
    var documents: String? = "",
    var checked: String? = "",
    var calendar: String? = "",
    var date: String? = ""
)

data class PaymentRegisterModel(
    val id: Int,
    val clientId: String,
    val branch: String,
    val date: String,
    val clientName: String,
    val documentNumber: String,
    val type: String,
    val total: Double
)

data class ClientModel(
    val clientId: String,
    val clientName: String,
)

data class InfoClientModel(
    val clientId: Int,
    var clientName: String,
    var fantasyName: String,
    var cuit: String,
    var address: String,
    var taxAddress: String,
    var city: String,
    var taxCity: String,
    var deliveryTime: String,
    var email: String,
    var phone: String,
    var contactName: String
)

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

    // 1. Determinar Fecha de Pago
    val newPayDate = if (reception != null && condition != null) {
        reception.plusDays(condition.expiration.toLong()).format(formatter)
    } else if (paymentCondition.isEmpty() && condition == null) {
        ""
    } else {
        payDate // Preservar si ya existe
    }

    val today = LocalDate.now()
    val parsedPayDate = try {
        if (newPayDate.isNotEmpty()) LocalDate.parse(newPayDate, formatter) else null
    } catch (e: Exception) {
        null
    }

    // 2. Determinar Estado (Reglas de Negocio)
    val newState = when {
        // Regla prioritaria: Cobrado si no hay saldo
        restValue <= 0 && toPayValue > 0 -> "Cobrado"

        // Respetar estados manuales finales
        stateBilling == "Cerrada" || stateBilling == "Devuelta" || stateBilling == "Cancelado" -> stateBilling

        // Lógica basada en tiempo (Vencimientos)
        parsedPayDate != null && restValue > 0 -> {
            val daysUntilDue = ChronoUnit.DAYS.between(today, parsedPayDate)
            when {
                today.isAfter(parsedPayDate) -> "Vencido"
                daysUntilDue <= 1 -> "Por vencer" // Hoy o mañana
                else -> {
                    // Si no es por vencer aún, puede ser "En proceso" si hay pagos, o "Pendiente"
                    if (payed > 0) "En proceso" else "Pendiente"
                }
            }
        }

        // Si no hay fecha de pago pero hay movimientos
        payed > 0 && restValue > 0 -> "En proceso"

        // Estado por defecto
        else -> if (stateBilling.isEmpty()) "Pendiente" else stateBilling
    }

    return copy(
        toPay = toPayValue,
        rest = restValue,
        payDate = newPayDate,
        stateBilling = newState
    )
}

fun LocalDate.formatter(): String {
    return this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

