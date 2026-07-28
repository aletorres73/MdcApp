package com.mdcapp.domain.entities

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class BuyOrderModel(
    var id: String = "",
    var clientId: String = "",
    var order: String = "",
    var client: String = "",
    var factory: String = "",
    var branch: String = "",
    var deliveryDate: Long = 0L,
    var type: String = "",
    var billing: String = "",
    var comments: String = "",
    var articles: List<ArticleOrderModel> = emptyList(),
    var loadedDate: Long = 0L,
    var paymentCondition: String = "",
    var discount: Double = 0.0,
    var expirationDays: Int = 0,
    var timeStamp: Long = 0L
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
    var documentDate: Long = 0L,
    var numberDocument: String = "",
    var trackingState: String = "",
    var documentComments: String = "",
    var sellOut: String = "",
    var inputDate: Long = 0L,
    var payState: String = "",
    var receptionDate: Long = 0L,
    var payDate: Long = 0L,
    var valueDocument: String = "",
    var discount: String = "",
    var payAmount: String = "",
    var payedAmount: String = "",
    var payDifference: String = "",
    var orders: String = "",
    var documents: String? = "",
    var checked: String? = "",
    var calendar: String? = "",
    var date: Long = 0L
)

data class PaymentRegisterModel(
    val id: Int,
    val clientId: String,
    val branch: String,
    val date: Long = 0L,
    val clientName: String,
    val documentNumber: String,
    val type: String,
    val total: Double,
    val notes: String = "",
    val method: String = "PAGO", // EFECTIVO, TRANSFERENCIA, PRONTO_PAGO, NOTA_CREDITO, DESCUENTO_EXTRA
    val status: String = "PENDIENTE_FABRICA", // PENDIENTE_FABRICA, IMPUTADO_FABRICA
    val reconciliationDate: Long = 0L,
    val isVirtual: Boolean = false
)

data class ClientModel(
    val clientId: String,
    val clientName: String,
)

data class BillingModel(
    val billingNumber: String = "",
    val orderId: String = "",
    val type: String = "",
    val total: Double = 0.0,
    val loadDate: Long = 0L,
    val deliveryDate: Long = 0L,
    val payDate: Long = 0L,
    val articles: List<ArticleModel> = emptyList(),
    val paymentCondition: String = "",
    val expectedDiscount: Double = 0.0,
    val toPay: Double = 0.0,
    val payed: Double = 0.0,
    val rest: Double = 0.0,
    val stateBilling: String = "",
    val clientId: String = "",
    val brand: String = "",
    val branch: String = "",
    val comments: List<BillingComments> = emptyList(),
    val clientName: String = "",
    val timeStamp: Long = 0L
)

data class BillingComments(
    val comments: String = "",
    val date: Long = 0L
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

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
}

fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
}

fun Long.toFormattedDate(): String {
    if (this == 0L) return "---"
    return this.toLocalDate().formatter()
}

fun BillingModel.recalculate(
    condition: PaymentCondition? = null
): BillingModel {

    // El monto a cobrar (toPay) ahora es siempre el total bruto.
    // Los descuentos se manejan como movimientos en el historial.
    val toPayValue = total 
    val restValue = toPayValue - payed

    val reception = if (deliveryDate != 0L) deliveryDate.toLocalDate() else null

    // 1. Determinar Fecha de Pago
    val newPayDate = if (reception != null && condition != null) {
        reception.plusDays(condition.expiration.toLong()).toEpochMillis()
    } else if (paymentCondition.isEmpty() && condition == null) {
        0L
    } else {
        payDate // Preservar si ya existe
    }

    val today = LocalDate.now()
    val parsedPayDate = if (newPayDate != 0L) newPayDate.toLocalDate() else null

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

