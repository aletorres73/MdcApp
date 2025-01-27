package com.mdcapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PaymentRegisterModel(
    val id: Int,
    val clientId: String,
    val branch: String,
    val date: String,
    val clientName: String,
    val documentNumber: String,
    val type: String,
//    val iva: Double,
//    val commission: Double,
    val total: Double
)

fun PaymentRegisterModel.toDomain() = RemotePaymentRegisterResult(
    id, clientId, branch, date, clientName, documentNumber, type, total
)

@Serializable
data class RemotePaymentRegisterResult(
    @SerialName("Pago Id") val id: Int = 0,
    @SerialName("Cliente ID") val clientId: String = "",
    @SerialName("Marca") val branch: String = "",
    @SerialName("Fecha") val date: String = "",
    @SerialName("Razón Social") val clientName: String = "",
    @SerialName("Remito") val documentNumber: String = "",
    @SerialName("Tipo") val type: String = "",
    @SerialName("Monto Pagado") val total: Double = 0.0,
    /*
        @SerialName("iva") val iva: Double = 0.0,
        @SerialName("Comisión %") val commission: Double = 0.0*/
)

fun RemotePaymentRegisterResult.toDomain() = PaymentRegisterModel(
    id, clientId, branch, date, clientName, documentNumber, type, total
)