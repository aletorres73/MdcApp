package com.mdcapp.data.remote

import com.mdcapp.domain.entities.FactoryModel
import com.mdcapp.domain.entities.PaymentCondition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultFactoryModel(
    @SerialName("Fabrica") val name: String = "",
    @SerialName("Marcas") val branchList: List<String> = emptyList(),
    @SerialName("Condiciones") val paymentsTypes: Map<String, Map<String, String>> = emptyMap()
)

fun RemoteResultFactoryModel.toFactoryDomain() = FactoryModel(
    name = name,
    branchList = branchList,
    paymentType = paymentsTypes.toPaymentConditions()
)

fun FactoryModel.toFactoryRemote() = RemoteResultFactoryModel(
    name = name,
    branchList = branchList,
    paymentsTypes = paymentType.toRemoteMap()
)

fun Map<String, Map<String, Any>>.toPaymentConditions(): List<PaymentCondition> {
    return this.values.map { paymentInfo ->
        PaymentCondition(
            paymentName = paymentInfo["condicion"]?.toString() ?: "Sin condición",
            discount = paymentInfo["dto"]?.toString()?.toDoubleOrNull() ?: 0.0,
            month = paymentInfo["meses"]?.toString()?.toIntOrNull() ?: 0,
            expiration = paymentInfo["vencimiento"]?.toString()?.toIntOrNull() ?: 0,
            date = paymentInfo["plazo"]?.toString()?.toIntOrNull() ?: 0,
            quantity = paymentInfo["pagos"]?.toString()?.toIntOrNull() ?: 0
        )
    }
}

fun List<PaymentCondition>.toRemoteMap(): Map<String, Map<String, String>> {
    return this.mapIndexed { index, paymentCondition ->
        "condicion${index + 1}" to mapOf(
            "condicion" to paymentCondition.paymentName,
            "dto" to paymentCondition.discount.toString(),
            "meses" to paymentCondition.month.toString(),
            "vencimiento" to paymentCondition.expiration.toString(),
            "plazo" to paymentCondition.date.toString(),
            "pagos" to paymentCondition.quantity.toString()
        )
    }.toMap()
}

