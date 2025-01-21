package com.mdcapp.data.remote

import com.mdcapp.data.model.FactoryModel
import com.mdcapp.data.model.PaymentCondition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultFactoryModel(
    @SerialName("Fabrica") val name: String = "",
    @SerialName("Marcas") val branchList: List<String> = emptyList(),
    @SerialName("Condiciones") val paymentsTypes: Map<String, Map<String, String>> = emptyMap()
)

fun RemoteResultFactoryModel.toDomain() = FactoryModel(
    name = name,
    branchList = branchList,
    paymentType = paymentsTypes.toPaymentConditions()
)

fun Map<String, Map<String, Any>>.toPaymentConditions(): List<PaymentCondition> {
    return this.values.map { paymentInfo ->
        PaymentCondition(
            paymentName = paymentInfo["condicion"].toString(),
            discount = paymentInfo["dto"].toString().toDouble(),
            month = paymentInfo["meses"].toString().toInt(),
            expiration = paymentInfo["vencimiento"].toString().toInt(),
            date = paymentInfo["plazo"].toString().toInt()
        )
    }
}